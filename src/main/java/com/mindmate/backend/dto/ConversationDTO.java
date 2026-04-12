package com.mindmate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
    private Long id;
    private String title;        // චැට් එකේ මාතෘකාව (උදා: "Feeling Lonely")
    private LocalDateTime createdAt;
}
