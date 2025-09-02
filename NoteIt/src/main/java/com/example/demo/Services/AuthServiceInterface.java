package com.example.demo.Services;

import com.example.demo.Dtos.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


public interface AuthServiceInterface {
    ApiResponse<String> signup(SignUpReq sign);

    ApiResponse<LoginResponseDto> login(LoginReq loginRequest, HttpServletResponse response);

    ApiResponse<String> verifyOtpAndSaveUser(String email, String otp, SignUpReq signUpReq);

    ApiResponse<String> refreshAccessToken(String refreshToken);
}
