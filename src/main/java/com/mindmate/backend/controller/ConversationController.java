package com.mindmate.backend.controller;

import com.mindmate.backend.dto.ConversationDTO;
import com.mindmate.backend.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ConversationController {

    private final ConversationService conversationService;


    @PostMapping("/create")
    public ResponseEntity<ConversationDTO> createConversation(@RequestParam String clerkId) {

        return ResponseEntity.ok(conversationService.createConversation(clerkId));
    }


    @GetMapping("/user/{clerkId}")
    public ResponseEntity<List<ConversationDTO>> getMyConversations(@PathVariable String clerkId) {
        return ResponseEntity.ok(conversationService.getConversationsByUser(clerkId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTitle(@PathVariable String id, @RequestBody String newTitle) {
        conversationService.updateConversationTitle(id, newTitle);
        return ResponseEntity.ok("Title updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteConversation(@PathVariable String id) {
        conversationService.deleteConversation(id);
        return ResponseEntity.ok("Conversation deleted");
    }
}