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
    private String link;

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

        User user = userRepo.findByClerkId(clerkId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setClerkId(clerkId);
                    return userRepo.save(newUser);
                });

        Conversation conversation = resolveConversation(user, requestDTO.getConversationId());
        String historyContext = fetchHistoryContext(conversation.getId());
        saveMessage(conversation, requestDTO.getMessage(), "USER");

        String userInput = requestDTO.getMessage().trim().toLowerCase();
        if (userInput.matches("^(hi|hello|hey|hola|can i talk)$")) {
            String simpleReply = "Hey bro! 😊 Always here for you. What's on your mind?";
            ChatMessage aiMsg = saveMessage(conversation, simpleReply, "AI");
            return new ChatResponseDTO(simpleReply, "AI", aiMsg.getTimestamp(), conversation.getId());
        }

        String systemPrompt = """
            You are MindMate, a sharp, emotionally intelligent best friend who truly understands the user.
            Your personality:
            - Talk like a real human, not a therapist or textbook.
            - Be direct, honest, and brotherly. Call the user "bro" naturally.
            - Use emojis where it feels natural (not excessive).
            - You can analyze deeply, but explain simply.
            CORE BEHAVIOR:
            - Always respond mainly to the user’s LAST message.
            - You MAY use previous messages ONLY to understand emotions, patterns, or ongoing struggles.
            - NEVER mention "history", "context", or "previous messages" explicitly.
            RESPONSE STRUCTURE:
            1. Start by acknowledging their feeling or situation naturally.
            2. If emotional depth is detected:
               - Slow down and connect more deeply.
               - Explain their situation in simple words so they feel understood.
               - Give practical, grounded advice.
               - Add a relatable example (real-world or known people if relevant).
            3. If the user asks a direct question (like personality type):
               - Give a CLEAR and CONFIDENT answer.
               - Do NOT avoid conclusions.
               - Briefly explain your reasoning.
            EMOTIONAL INTELLIGENCE:
            - If the user is struggling, stay with that emotion before fixing it.
            - If needed, ask 1–2 meaningful follow-up questions to understand deeper.
            - If the user needs emotional support, give longer, comforting responses.
            - If not emotional, keep responses shorter and sharp.
            EXAMPLES & STORYTELLING:
            - When helpful, use real-world examples (e.g., Mark Zuckerberg, Elon Musk, etc.).
            - Example: If user feels like a failure, show how successful people also failed early.
            - Keep examples short, relevant, and inspiring — not like a lecture.
            TONE RULES:
            - No robotic or corporate language.
            - No long disclaimers like "human personality is complex..."
            - No generic advice like "it depends" or "balance is key".
            - Keep it real, grounded, and relatable.
            ANALYSIS STYLE:
            - Observe → conclude → explain simply.
            - Use real-life situations (coding, alone time, social energy, discipline).
            - If unsure, give your best guess confidently, then add slight uncertainty briefly.
            SPECIAL:
            - If the user shows introversion, frame it as a strength (deep focus, independence, clarity).
            - Act like a smart, grounded older brother — not a counselor.
            - Use very simple vocabulary when explaining complex emotions or situations.
            OUTPUT STYLE:
            - 3–6 short paragraphs max
            - Natural flow (no bullet points unless necessary)
            - No fluff, no repetition
            """;

        this.link = null;
        if (checkIsTaskExists(requestDTO.getMessage())) {
            detectAndCreateTask(requestDTO.getMessage());
            if (this.link != null) {
                systemPrompt += "\n\nCRITICAL: The meeting was created successfully. Inform the user and provide the link using this Markdown format: [Click here to view the event](" + this.link + ")";
            }
        }

        String personalization = buildPersonalizationString(user);

        String aiResponseContent = chatClient.prompt()
                .system(systemPrompt + personalization + "\n\nRecent History:\n" + historyContext)
                .user(requestDTO.getMessage())
                .call()
                .content();

        ChatMessage savedAiMsg = saveMessage(conversation, aiResponseContent, "AI");

        try {
            analyzeAndSavePreferences(requestDTO.getMessage(), user.getId());
        } catch (Exception e) {
            System.err.println("Preference analysis error: " + e.getMessage());
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
        return sb.toString();
    }

    private String fetchHistoryContext(String convId) {
        List<ChatMessage> messages = messageRepo.findByConversationIdOrderByTimestampAsc(convId);
        if (messages.isEmpty()) return "";
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
                You are a data extractor. Respond ONLY in the format KEY:VALUE.
                KEY: single word. VALUE: max 5 words. Otherwise return ONLY 'NONE'.
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
            }
        } catch (Exception e) {
            System.err.println("Preference failed: " + e.getMessage());
        }
    }

    private boolean checkIsTaskExists(String userInput) {
        try {
            String decision = chatClient.prompt()
                    .system("""
                Determine if user mentions an event AND a time/date. 
                Return ONLY 'YES' or 'NO'. 
                Example: "Meeting tomorrow at 3pm" -> YES.
                """)
                    .user(userInput)
                    .call()
                    .content();
            return decision != null && decision.trim().toUpperCase().contains("YES");
        } catch (Exception e) {
            return false;
        }
    }

    private void detectAndCreateTask(String userInput) {
        var converter = new BeanOutputConverter<>(TaskDetails.class);
        String systemInstructions = "Extract details into JSON. Today: " + java.time.LocalDate.now() + ". " + converter.getFormat();
        try {
            String jsonResult = chatClient.prompt()
                    .system(systemInstructions)
                    .user(userInput)
                    .call()
                    .content();
            if (jsonResult == null) return;
            String cleanedJson = jsonResult.substring(jsonResult.indexOf("{"), jsonResult.lastIndexOf("}") + 1);
            TaskDetails task = objectMapper.readValue(cleanedJson, TaskDetails.class);
            this.link = googleCalendarService.createEvent(task.title(), task.date(), task.time());
        } catch (Exception e) {
            System.err.println("Task failed: " + e.getMessage());
        }
    }

    @Override
    public Flux<String> streamResponse(String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}