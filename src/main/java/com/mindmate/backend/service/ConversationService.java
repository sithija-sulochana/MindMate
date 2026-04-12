package com.mindmate.backend.service;

import com.mindmate.backend.dto.ConversationDTO;

import java.util.List;

public interface ConversationService {
    ConversationDTO createConversation(String clerkId);
    List<ConversationDTO> getConversationsByUser(String clerkId);
    void updateConversationTitle(Long conversationId, String newTitle);
    void deleteConversation(Long conversationId);

}
