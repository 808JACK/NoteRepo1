package com.example.demo.Dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private Long userId;
    private String username;
    private String displayName;
    private String email;
    private String accessToken;
    private String refreshToken;

    public LoginResponseDto(String refreshToken, String accessToken, String username) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.username = username;
    }
}
