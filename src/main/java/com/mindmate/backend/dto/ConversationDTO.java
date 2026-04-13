package com.mindmate.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDTO {
<<<<<<< HEAD
    private String id;
    private String title;        // චැට් එකේ මාතෘකාව (උදා: "Feeling Lonely")
=======
    private Long id;
    private String title;       
>>>>>>> ad771267ec4ff8631daa4487176924d52552ce32
    private LocalDateTime createdAt;
}
