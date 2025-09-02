package com.example.demo.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {
    private Long id;
    private Long userId;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private String customStatus;
    private Long createdAt;
    private Long updatedAt;
}