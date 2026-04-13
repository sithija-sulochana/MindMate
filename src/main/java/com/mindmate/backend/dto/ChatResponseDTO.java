package com.mindmate.backend.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
public class ChatResponseDTO {
    private String content;
    private String sender;
    private LocalDateTime timestamp;
    private String conversationId;

    public ChatResponseDTO(String content, String sender, LocalDateTime timestamp, String conversationId) {
        this.content = content;
        this.sender = sender;
        this.timestamp = timestamp;
        this.conversationId = conversationId;
    }


}