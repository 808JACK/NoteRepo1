package com.example.demo.Controllers;

import com.example.demo.Dtos.LoginResponseDto;
import com.example.demo.Services.AuthServiceImpl;
import com.example.demo.Services.RefreshTokenService;
import com.example.demo.Services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class TokenController {

    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final AuthServiceImpl authService;

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        // Remove "Bearer " prefix if present
        String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
        return refreshTokenService.handleRefresh(token);
    }

    @PostMapping("/revoke")
    public ResponseEntity<Void> revokeToken(@RequestHeader("Authorization") String refreshToken) {
        try {
            // Remove "Bearer " prefix if present
            String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
            refreshTokenService.revokeRefreshToken(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error revoking token: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix if present
            String accessToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            boolean isValid = jwtService.validateToken(accessToken);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/user-info")
    public ResponseEntity<UserInfo> getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // Remove "Bearer " prefix if present
            String accessToken = token.startsWith("Bearer ") ? token.substring(7) : token;
            var user = jwtService.getUserFromToken(accessToken);
            return ResponseEntity.ok(new UserInfo(user.getId().toString(), user.getEmail()));
        } catch (Exception e) {
            log.error("Error getting user info: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/validate-both")
    public ResponseEntity<Boolean> validateBothTokens(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("Refresh-Token") String refreshToken) {
        try {
            // Remove "Bearer " prefix if present
            String accessTokenValue = accessToken.startsWith("Bearer ") ? accessToken.substring(7) : accessToken;
            String refreshTokenValue = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;

            // Validate access token
            boolean isAccessTokenValid = jwtService.validateToken(accessTokenValue);
            
            // Validate refresh token
            boolean isRefreshTokenValid = refreshTokenService.findByToken(refreshTokenValue)
                    .map(token -> {
                        try {
                            refreshTokenService.verifyExpiration(token);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .orElse(false);

            // Both tokens must be valid
            return ResponseEntity.ok(isAccessTokenValid && isRefreshTokenValid);
        } catch (Exception e) {
            log.error("Error validating tokens: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    @GetMapping("/refreshAT/{userId}")
    public ResponseEntity<String> refreshAT(@PathVariable Long userId) {
        try {
            log.info("Attempting to refresh access token for user: {}", userId);
            String result = authService.refreshAT(userId);
            log.info("Successfully refreshed token for user: {}", userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error refreshing access token for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(500).body("REFRESH_ERROR");
        }
    }

    public record UserInfo(String id, String email) {}
} 