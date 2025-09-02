package com.example.demo.Entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes")
public class Note {
    
    @Id
    private String id;
    
    private String title;
    
    private String content;
    
    private String userId; // Reference to user from PostgreSQL
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private String category;
    
    private boolean isArchived;
    
    public Note(String title, String content, String userId) {
        this.title = title;
        this.content = content;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isArchived = false;
    }
}