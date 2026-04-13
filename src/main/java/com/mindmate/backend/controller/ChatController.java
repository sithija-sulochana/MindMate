package com.mindmate.backend.controller;

import com.mindmate.backend.dto.ChatRequestDTO;
import com.mindmate.backend.dto.ChatResponseDTO;
import com.mindmate.backend.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.security.Principal;

@RestController
@RequestMapping("/api/llm")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;


    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    @GetMapping("/test")
    public ResponseEntity<String> testChat(@RequestParam String message) {

        return ResponseEntity.ok("Service is working!");
    }

    @PostMapping("/send")
    public ResponseEntity<ChatResponseDTO> sendMessage(@RequestBody ChatRequestDTO requestDTO, Principal principal) {


        String clerkId = (principal != null) ? principal.getName() : "test_clerk_id";

        ChatResponseDTO response = chatService.generateResponse(requestDTO, clerkId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestBody ChatRequestDTO request) {
        return chatService.streamResponse(request.getMessage());
    }
}