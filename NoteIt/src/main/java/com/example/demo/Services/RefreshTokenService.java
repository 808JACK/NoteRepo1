package com.example.demo.Services;

import com.example.demo.Dtos.ApiResponse;
import com.example.demo.Entities.RefreshToken;
import com.example.demo.Entities.User;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repo.jpa.AuthRepo;
import com.example.demo.Repo.jpa.RefreshTokenRepository;
import com.example.demo.Dtos.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    @Autowired
    @Lazy
    private JwtService jwtService;
    private final AuthRepo authRepo;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Generate JWT refresh token
        String token = jwtService.generateRefreshToken(user);
        
        // Create and save refresh token entity
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .map(existingToken -> {
                    existingToken.setToken(token);
                    existingToken.setExpiryDate(Instant.now().plusMillis(jwtService.getRefreshExpiration()));
                    existingToken.setRevoked(false);
                    existingToken.setCreatedAt(Instant.now());
                    return existingToken;
                })
                .orElse(RefreshToken.builder()
                        .user(user)
                        .token(token)
                        .expiryDate(Instant.now().plusMillis(jwtService.getRefreshExpiration()))
                        .revoked(false)
                        .createdAt(Instant.now())
                        .build());

        return refreshTokenRepository.save(refreshToken);
    }

    public Boolean verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0 || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            return false;
        }
        return true;
    }
//use for accidental logout
    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
//not usefull method instead do login
    public ResponseEntity<LoginResponseDto> handleRefresh(String token) {
        try {
            RefreshToken storedToken = findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));
            verifyExpiration(storedToken);

            User user = storedToken.getUser();
            String newAccessToken = jwtService.generateAccessToken(user);
            RefreshToken newRefreshToken = createRefreshToken(user);

            ResponseCookie jwtCookie = ResponseCookie.from("token", newAccessToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            LoginResponseDto response = new LoginResponseDto(
                    newRefreshToken.getToken(),
                    null,
                    user.getUsername()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(response);

        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(new LoginResponseDto(null, null, null));
        }
    }

} 