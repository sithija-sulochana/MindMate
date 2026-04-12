package com.mindmate.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindmate.backend.Repository.ChatMessageRepository;
import com.mindmate.backend.Repository.ConversationRepository;
import com.mindmate.backend.Repository.UserPreferenceRepository;
import com.mindmate.backend.dto.ChatRequestDTO;
import com.mindmate.backend.dto.ChatResponseDTO;
import com.mindmate.backend.dto.UserPreferenceDTO;
import com.mindmate.backend.entities.ChatMessage;
import com.mindmate.backend.entities.Conversation;
import com.mindmate.backend.entities.UserPreference;
import com.mindmate.backend.service.ChatService;
import com.mindmate.backend.service.UserPreferenceService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final GoogleCalendarServiceImpl googleCalendarService;
    private final ConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;
    private final UserPreferenceRepository preferenceRepo;
    private final UserPreferenceService preferenceService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public ChatServiceImpl(ChatClient.Builder builder,
                           GoogleCalendarServiceImpl googleCalendarService,
                           ConversationRepository conversationRepo,
                           ChatMessageRepository messageRepo,
                           UserPreferenceRepository preferenceRepo,
                           UserPreferenceService preferenceService) { // Inject preferenceService
        this.chatClient = builder.build();
        this.googleCalendarService = googleCalendarService;
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.preferenceRepo = preferenceRepo;
        this.preferenceService = preferenceService;
    }

    public String ask(String userInput) {
        return chatClient.prompt()
                .system("You are MindMate, a supportive and empathetic best friend. Never mocking.")
                .user(userInput)
                .call()
                .content();
    }

    public record TaskDetails(String title, String date, String time) {}

    @Override
    @Transactional
    public ChatResponseDTO generateResponse(ChatRequestDTO requestDTO, String clerkId) {
        Conversation conversation = conversationRepo.findById(requestDTO.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        saveMessage(conversation, requestDTO.getMessage(), "USER");

        Long userId = conversation.getUser() != null ? conversation.getUser().getId() : null;
        String systemPrompt = buildDynamicSystemPrompt(userId);
        String historyContext = fetchHistoryContext(conversation.getId());

        if (checkIsTaskExists(requestDTO.getMessage())) {
            detectAndCreateTask(requestDTO.getMessage());
        }

        String aiResponseContent = chatClient.prompt()
                .system(systemPrompt + "\n\nRecent History:\n" + historyContext)
                .user(requestDTO.getMessage())
                .call()
                .content();

        ChatMessage savedAiMsg = saveMessage(conversation, aiResponseContent, "AI");

        if (userId != null) {
            analyzeAndSavePreferences(requestDTO.getMessage(), userId);
        }

        return new ChatResponseDTO(
                aiResponseContent,
                "AI",
                savedAiMsg.getTimestamp(),
                conversation.getId()
        );
    }

    private String buildDynamicSystemPrompt(Long userId) {
        List<UserPreference> prefs = userId == null ? Collections.emptyList() : preferenceRepo.findByUserId(userId);
        StringBuilder sb = new StringBuilder("You are MindMate, a supportive and empathetic best friend. ");

        if (!prefs.isEmpty()) {
            sb.append("Consider these user traits: ");
            prefs.forEach(p -> sb.append(p.getPrefKey()).append(" is ").append(p.getPrefValue()).append(". "));
        }
        return sb.toString();
    }

    private String fetchHistoryContext(Long convId) {
        List<ChatMessage> messages = messageRepo.findByConversationIdOrderByTimestampAsc(convId);
        if (messages.isEmpty()) {
            return "";
        }

        int startIndex = Math.max(0, messages.size() - 5);
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
        String analysis = chatClient.prompt()
                .system("""
                Extract psychological traits from the user's message. 
                Format: KEY:VALUE. 
                Example: personality:introvert. 
                If none, return 'NONE'.
                """)
                .user(userMessage)
                .call()
                .content();

        if (analysis != null && !analysis.contains("NONE") && analysis.contains(":")) {
            try {
                String[] parts = analysis.split(":");
                String key = parts[0].trim().toLowerCase();
                String value = parts[1].trim().toLowerCase();

                // DTO එක සාදා දත්ත පුරවන්න
                UserPreferenceDTO dto = new UserPreferenceDTO();
                dto.setPrefKey(key);
                dto.setPrefValue(value);

                // Service එක හරහා Save කරන්න
                // සටහන: ඔයාගේ UserPreferenceService එකේ userId එකෙන් save කරන method එකක් තියෙන්න ඕනේ
                preferenceService.updatePreferenceByUserId(userId, dto);

                System.out.println("✅ Trait Extracted & Saved: " + key + " = " + value);
            } catch (Exception e) {
                System.err.println("❌ Error processing preference: " + e.getMessage());
            }
        }
    }

    private boolean checkIsTaskExists(String userInput) {
        String decision = chatClient.prompt()
                .system("Analyze if this is a schedule/reminder. Return ONLY 'YES' or 'NO'.")
                .user(userInput)
                .call()
                .content();
        return decision != null && decision.trim().equalsIgnoreCase("YES");
    }

    private void detectAndCreateTask(String userInput) {
        var converter = new BeanOutputConverter<>(TaskDetails.class);
        String systemInstructions = "Extract task details into JSON. Today is Sunday, 2026-04-12. " + converter.getFormat();

        try {
            String jsonResult = chatClient.prompt()
                    .system(systemInstructions)
                    .user(userInput)
                    .call()
                    .content();

            if (jsonResult == null) {
                return;
            }

            String cleanedJson = jsonResult.trim();
            int start = cleanedJson.indexOf("{");
            int end = cleanedJson.lastIndexOf("}");
            if (start >= 0 && end > start) {
                cleanedJson = cleanedJson.substring(start, end + 1);
                TaskDetails task = objectMapper.readValue(cleanedJson, TaskDetails.class);

                if (task != null) {
                    googleCalendarService.createEvent(task.title(), task.date(), task.time());
                }
            }
        } catch (Exception e) {
            System.err.println("Task Parsing Error: " + e.getMessage());
        }
    }
}