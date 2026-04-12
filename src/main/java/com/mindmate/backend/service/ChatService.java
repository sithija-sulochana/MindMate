package com.mindmate.backend.service;

import com.mindmate.backend.dto.ChatRequestDTO;
import com.mindmate.backend.dto.ChatResponseDTO;

public interface ChatService {
    ChatResponseDTO generateResponse(ChatRequestDTO requestDTO, String clerkId);
    void analyzeAndSavePreferences(String userMessage, Long userId);
}
