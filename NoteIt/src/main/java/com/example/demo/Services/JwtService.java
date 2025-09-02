package com.example.demo.Services;

import com.example.demo.Dtos.ApiResponse;
import com.example.demo.Entities.RefreshToken;
import com.example.demo.Entities.User;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repo.jpa.AuthRepo;
import com.example.demo.Repo.jpa.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {

    private final AuthRepo authRepo;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey key() {
        try {
            byte[] keyBytes = hexStringToByteArray(jwtSecretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("Error creating secret key: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create JWT secret key", e);
        }
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Getter
    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username",user.getUsername())
                .claim("email", user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username",user.getUsername())
                .claim("email", user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key())
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.valueOf(claims.getSubject());
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }
//Usefull to validate refresh token and accesstoken
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    public User getUserFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.valueOf(claims.getSubject());
            return authRepo.findUserById(userId);
        } catch (Exception e) {
            log.error("Error getting user from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token");
        }
    }

    public String generateAccess(String token,Long userId,String userName,String email) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username",userName)
                .claim("email", email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key())
                .compact();
    }

}
