package com.mindmate.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private String content;      // පණිවිඩය
    private String sender;       // "USER" හෝ "AI"
    private LocalDateTime timestamp;
    private Long conversationId; // අදාළ conversation එකේ ID එක විතරයි
}