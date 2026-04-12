package com.mindmate.backend.service.impl;



import com.mindmate.backend.dto.ConversationDTO;
import com.mindmate.backend.entities.Conversation;
import com.mindmate.backend.entities.User;
import com.mindmate.backend.Repository.ConversationRepository;
import com.mindmate.backend.Repository.UserRepository;
import com.mindmate.backend.service.ConversationService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepo;
    private final UserRepository userRepo;
    private final ModelMapper modelMapper;

    public ConversationServiceImpl(ConversationRepository conversationRepo,
                                   UserRepository userRepo,
                                   ModelMapper modelMapper) {
        this.conversationRepo = conversationRepo;
        this.userRepo = userRepo;
        this.modelMapper = modelMapper;
    }

    @Override
    @Transactional
    public ConversationDTO createConversation(String clerkId) {
        User user = userRepo.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found with Clerk ID: " + clerkId));

        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setTitle("New Chat " + (conversationRepo.findByUserOrderByCreatedAtDesc(user).size() + 1));

        Conversation savedConversation = conversationRepo.save(conversation);
        return modelMapper.map(savedConversation, ConversationDTO.class);
    }

    @Override
    public List<ConversationDTO> getConversationsByUser(String clerkId) {
        User user = userRepo.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Conversation> conversations = conversationRepo.findByUserOrderByCreatedAtDesc(user);

        return conversations.stream()
                .map(conv -> modelMapper.map(conv, ConversationDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateConversationTitle(Long conversationId, String newTitle) {
        Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        conversation.setTitle(newTitle);
        conversationRepo.save(conversation);
    }

    @Override
    @Transactional
    public void deleteConversation(Long conversationId) {
        conversationRepo.deleteById(conversationId);
    }
}