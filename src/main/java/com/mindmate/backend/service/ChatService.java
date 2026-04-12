package com.mindmate.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final GoogleCalendarService googleCalendarService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatService(ChatClient.Builder builder, GoogleCalendarService googleCalendarService) {
        this.chatClient = builder.build();
        this.googleCalendarService = googleCalendarService;
    }

    public record TaskDetails(String title, String date, String time) {}

    public String getResponse(String userInput) {
        String aiResponse = chatClient.prompt()
                .system("You are MindMate, a supportive and empathetic best friend. Never mocking.")
                .user(userInput)
                .call()
                .content();

        try {
            detectAndCreateTask(userInput);
        } catch (Exception e) {
            System.err.println("Task detection failed: " + e.getMessage());
        }

        return aiResponse;
    }

    private void detectAndCreateTask(String userInput) {
        var converter = new BeanOutputConverter<>(TaskDetails.class);


        String systemInstructions = """
            Extract task details into JSON. 
            RULES:
            1. Use ONLY English.
            2. Date must be YYYY-MM-DD.
            3. Time must be HH:mm (24-hour format only, NO AM/PM).
            4. Today is Sunday, 2026-04-12.
            Return ONLY the JSON object.
            """ + converter.getFormat();

        try {
            String jsonResult = chatClient.prompt()
                    .system(systemInstructions)
                    .user(userInput)
                    .call()
                    .content();

            if (jsonResult != null && !jsonResult.trim().equalsIgnoreCase("null")) {
                String cleanedJson = jsonResult.trim();
                if (cleanedJson.contains("{")) {
                    cleanedJson = cleanedJson.substring(cleanedJson.indexOf("{"), cleanedJson.lastIndexOf("}") + 1);
                }

                TaskDetails task = objectMapper.readValue(cleanedJson, TaskDetails.class);

                if (task != null && task.title() != null && task.date() != null) {


                    String cleanTime = task.time().replaceAll("(?i)\\s?[AP]M", "").trim();
                    if(!cleanTime.contains(":")) cleanTime = "09:00";

                    googleCalendarService.createEvent(
                            task.title(),
                            task.date().trim(),
                            cleanTime
                    );
                    System.out.println("✅ Event Created: " + task.title());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Parsing Error: " + e.getMessage());
        }
    }
}