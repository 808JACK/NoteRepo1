package com.example.demo.Controllers;

import com.example.demo.Dtos.ApiResponse;
import com.example.demo.Dtos.LoginReq;
import com.example.demo.Dtos.LoginResponseDto;
import com.example.demo.Dtos.SignUpReq;
import com.example.demo.Services.AuthServiceInterface;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthServiceInterface authServiceInterface;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signup(@Valid @RequestBody SignUpReq sign) {
        ApiResponse<String> response = authServiceInterface.signup(sign);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtpAndSaveUser(
            @RequestParam String email,
            @RequestParam String otp,
            @Valid @RequestBody SignUpReq signUpReq
    ) {
        ApiResponse<String> response = authServiceInterface.verifyOtpAndSaveUser(email, otp, signUpReq);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginReq loginRequest, HttpServletResponse response) {
        ApiResponse<LoginResponseDto> response1 = authServiceInterface.login(loginRequest,response);
        return ResponseEntity.ok(response1);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletResponse response) {
        // Clear any cookies that might have been set
        response.addHeader("Set-Cookie", "token=; Path=/; HttpOnly; Max-Age=0");
        response.addHeader("Set-Cookie", "refreshToken=; Path=/; HttpOnly; Max-Age=0");
        
        ApiResponse<String> apiResponse = ApiResponse.success("Logout successful", "Logged out successfully");
        
        log.info("[AuthController] User logged out successfully");
        return ResponseEntity.ok(apiResponse);
    }
}
