package com.mindmate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private String id;
    private String title;
    private LocalDateTime createdAt;
}
