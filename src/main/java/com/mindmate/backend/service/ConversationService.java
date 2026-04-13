package com.mindmate.backend.service;

import com.mindmate.backend.dto.ConversationDTO;

import java.util.List;

public interface ConversationService {
    ConversationDTO createConversation(String clerkId);
    List<ConversationDTO> getConversationsByUser(String clerkId);
    void updateConversationTitle(String conversationId, String newTitle);
    void deleteConversation(String conversationId);

}
