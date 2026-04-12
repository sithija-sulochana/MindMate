package com.mindmate.backend.controller;



import com.mindmate.backend.service.ChatService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/llm")

public class ChatController {



    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;

    }

//   private final ChatService chatService;
//
//   public ChatController(ChatService chatService){
//       this.chatService = chatService;
//   }
//
//
//   @GetMapping("/chat")
//   public String chat(@RequestParam String prompt ){
//       return chatService.ask(prompt);
//   }
@GetMapping
public String chat(@RequestParam String message) {
    return chatService.getResponse(message);
}




}