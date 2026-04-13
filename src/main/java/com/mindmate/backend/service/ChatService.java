package com.mindmate.backend.service;

import com.mindmate.backend.dto.ChatRequestDTO;
import com.mindmate.backend.dto.ChatResponseDTO;
import reactor.core.publisher.Flux;

public interface ChatService {
    ChatResponseDTO generateResponse(ChatRequestDTO requestDTO, String clerkId);
    void analyzeAndSavePreferences(String userMessage, Long userId);
    public Flux<String> streamResponse(String message);
}
