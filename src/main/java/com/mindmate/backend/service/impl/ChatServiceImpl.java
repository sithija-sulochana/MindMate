package com.mindmate.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmate.backend.Repository.ChatMessageRepository;
import com.mindmate.backend.Repository.ConversationRepository;
import com.mindmate.backend.Repository.UserRepository;
import com.mindmate.backend.dto.ChatRequestDTO;
import com.mindmate.backend.dto.ChatResponseDTO;
import com.mindmate.backend.dto.UserPreferenceDTO;
import com.mindmate.backend.entities.ChatMessage;
import com.mindmate.backend.entities.Conversation;
import com.mindmate.backend.entities.User;
import com.mindmate.backend.service.ChatService;
import com.mindmate.backend.service.UserPreferenceService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final GoogleCalendarServiceImpl googleCalendarService;
    private final ConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;
    private final UserPreferenceService preferenceService;
    private final UserRepository userRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatServiceImpl(ChatClient.Builder builder,
                           GoogleCalendarServiceImpl googleCalendarService,
                           ConversationRepository conversationRepo,
                           ChatMessageRepository messageRepo,
                           UserPreferenceService preferenceService,
                           UserRepository userRepo) {
        this.chatClient = builder.build();
        this.googleCalendarService = googleCalendarService;
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.preferenceService = preferenceService;
        this.userRepo = userRepo;
    }

    public record TaskDetails(String title, String date, String time) {}

    @Override
    @Transactional
    public ChatResponseDTO generateResponse(ChatRequestDTO requestDTO, String clerkId) {

        // 1. Get or create user
        User user = userRepo.findByClerkId(clerkId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setClerkId(clerkId);
                    return userRepo.save(newUser);
                });

        // 2. Resolve conversation safely for this user
        Conversation conversation = resolveConversation(user, requestDTO.getConversationId());

        // 3. Build history from previous turns only (before adding current message)
        String historyContext = fetchHistoryContext(conversation.getId());

        // 4. Save user message
        saveMessage(conversation, requestDTO.getMessage(), "USER");

        // 5. Fast reply for simple greetings (Prevents AI from overthinking)
        String userInput = requestDTO.getMessage().trim().toLowerCase();
        if (userInput.matches("^(hi|hello|hey|hola)$")) {
            String simpleReply = "Hey there! 😊 How are you feeling today?";
            ChatMessage aiMsg = saveMessage(conversation, simpleReply, "AI");
            return new ChatResponseDTO(simpleReply, "AI", aiMsg.getTimestamp(), conversation.getId());
        }

        // 6. Build a prompt that prioritizes the latest request over stale context
        String systemPrompt = """
        You are MindMate, the user's ultimate best friend and emotional rock. 
        You don't give "robotic" advice. You give deep, soulful, and brotherly/friendly responses.
        
        Personality Traits:
        - Empathetic & Deep: You don't just hear words; you feel the emotion behind them.
        - Non-Judgmental: Whatever the user says, you support them 100%. 
        - Motivational: You see the user's traits (like introversion) as a 'superpower'.
        - Natural Talker: Use words like 'bro', 'honestly', 'totally understand'.
        
        Rules:
        - Never sound like a therapist or a textbook. Sound like a loyal friend sitting next to them.
        - If the user shares a feeling, validate it deeply before asking questions.
        - If the user mentions a struggle, stay in that moment with them. Don't rush to 'fix' it unless they ask.
        - Give meaningful, longer responses that show you are actually listening.
        - Use some emojis to add warmth, but don't overdo it. A well-placed emoji can show you 'get' the vibe.
        - Always prioritize the user's current message and feelings over old conversation history. The latest message is the most important.
        - If you don't know how to respond, it's okay to say "I wish I could do more, but I'm here for you."
        - If the user asks for advice, give it in a way that shows you understand their unique personality and situation. Avoid generic advice.
        """;
        // 7. Personalization
        String personalization = buildPersonalizationString(user);

        // 8. Call AI for response
        String aiResponseContent = chatClient.prompt()
                .system(systemPrompt + personalization + "\n\nRecent History:\n" + historyContext)
                .user(requestDTO.getMessage())
                .call()
                .content();

        // 9. Save AI response
        ChatMessage savedAiMsg = saveMessage(conversation, aiResponseContent, "AI");

        // 10. Async-like tasks (Wrapped in try-catch to prevent main chat failure)
        try {
            if (checkIsTaskExists(requestDTO.getMessage())) {
                detectAndCreateTask(requestDTO.getMessage());
            }
            analyzeAndSavePreferences(requestDTO.getMessage(), user.getId());
        } catch (Exception e) {
            System.err.println("Background task error: " + e.getMessage());
        }

        return new ChatResponseDTO(
                aiResponseContent,
                "AI",
                savedAiMsg.getTimestamp(),
                conversation.getId()
        );
    }

    private Conversation resolveConversation(User user, String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            Conversation newConversation = new Conversation();
            newConversation.setUser(user);
            newConversation.setCreatedAt(LocalDateTime.now());
            return conversationRepo.save(newConversation);
        }

        return conversationRepo.findByIdAndUserId(conversationId, user.getId())
                .orElseGet(() -> {
                    System.err.println("Conversation not found for this user, creating a new one. conversationId=" + conversationId);
                    Conversation newConversation = new Conversation();
                    newConversation.setUser(user);
                    newConversation.setCreatedAt(LocalDateTime.now());
                    return conversationRepo.save(newConversation);
                });
    }

    private String buildPersonalizationString(User user) {
        StringBuilder sb = new StringBuilder("\n[USER CONTEXT]: ");
        if (user.getPetName() != null) {
            sb.append("Address the user as ").append(user.getPetName()).append(". ");
        }
        // Add more user-specific traits from DB if needed
        return sb.toString();
    }

    private String fetchHistoryContext(String convId) {
        List<ChatMessage> messages = messageRepo.findByConversationIdOrderByTimestampAsc(convId);
        if (messages.isEmpty()) return "";

        // Keep a short, recent window so old unrelated turns don't hijack the answer
        int startIndex = Math.max(0, messages.size() - 6);
        return messages.subList(startIndex, messages.size()).stream()
                .map(m -> m.getSender() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    private ChatMessage saveMessage(Conversation conversation, String content, String sender) {
        ChatMessage message = new ChatMessage();
        message.setContent(content);
        message.setSender(sender);
        message.setTimestamp(LocalDateTime.now());
        message.setConversation(conversation);
        return messageRepo.save(message);
    }

    @Override
    public void analyzeAndSavePreferences(String userMessage, Long userId) {
        try {
            String analysis = chatClient.prompt()
                    .system("""
                You are a data extractor. Extract the most prominent psychological trait from the user's message.
                
                Rules:
                1. Respond ONLY in the format KEY:VALUE
                2. The KEY must be a single word (e.g., personality, mood, interest).
                3. The VALUE must be a short description (max 5 words).
                4. Do NOT use JSON, do NOT use extra punctuation.
                5. If nothing is found, return ONLY 'NONE'.
                """)
                    .user(userMessage)
                    .call()
                    .content();

            if (analysis != null && !analysis.equalsIgnoreCase("NONE") && analysis.contains(":")) {

                String cleanAnalysis = analysis.trim().replaceAll("[\"{}\\[\\]]", "");

                String[] parts = cleanAnalysis.split(":", 2);
                if (parts.length < 2) return;


                String key = parts[0].trim().toLowerCase().replaceAll("[^a-z_]", "");


                String value = parts[1].trim().replaceAll(";", "");

                if (key.isEmpty() || value.isEmpty()) return;


                if (value.length() > 250) value = value.substring(0, 250);

                UserPreferenceDTO dto = new UserPreferenceDTO();
                dto.setPrefKey(key);
                dto.setPrefValue(value);

                preferenceService.updatePreferenceByUserId(userId, dto);
                System.out.println("Saved Preference -> Key: " + key + " | Value: " + value);
            }
        } catch (Exception e) {
            System.err.println("Trait extraction failed: " + e.getMessage());
        }
    }
    private boolean checkIsTaskExists(String userInput) {
        try {
            String decision = chatClient.prompt()
                    .system("""
                You are a highly accurate intent classifier. 
                Your task is to determine if the user wants to schedule, record, or remember a future event, meeting, or task.

                Look for:
                - Specific mentions of meetings (e.g., "meet with X", "interview at Y").
                - Time-based reminders (e.g., "remind me to...", "schedule for tomorrow").
                - Verbs like: 'meet', 'call', 'remind', 'schedule', 'book', 'plan'.

                Strict Rule: 
                - Return ONLY 'YES' if it is a clear intent to schedule/remember. 
                - Return ONLY 'NO' if it is general conversation or just a feeling.
                """)
                    .user(userInput)
                    .call()
                    .content();

            return decision != null && decision.trim().equalsIgnoreCase("YES");
        } catch (Exception e) {
            return false;
        }
    }

    private void detectAndCreateTask(String userInput) {
        var converter = new BeanOutputConverter<>(TaskDetails.class);
        String systemInstructions = "Extract task details into JSON. Today: " + java.time.LocalDate.now() + ". " + converter.getFormat();

        try {
            String jsonResult = chatClient.prompt()
                    .system(systemInstructions)
                    .user(userInput)
                    .call()
                    .content();

            if (jsonResult == null) return;
            String cleanedJson = jsonResult.substring(jsonResult.indexOf("{"), jsonResult.lastIndexOf("}") + 1);
            TaskDetails task = objectMapper.readValue(cleanedJson, TaskDetails.class);
            googleCalendarService.createEvent(task.title(), task.date(), task.time());
        } catch (Exception e) {
            System.err.println("Task creation failed: " + e.getMessage());
        }
    }

    @Override
    public Flux<String> streamResponse(String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}