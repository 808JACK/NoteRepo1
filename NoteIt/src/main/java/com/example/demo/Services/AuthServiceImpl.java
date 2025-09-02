package com.example.demo.Services;

import com.example.demo.Advice.ApiExceptions;
import com.example.demo.Dtos.ApiResponse;
import com.example.demo.Dtos.LoginReq;
import com.example.demo.Dtos.LoginResponseDto;
import com.example.demo.Dtos.SignUpReq;
import com.example.demo.Entities.Profile;
import com.example.demo.Entities.RefreshToken;
import com.example.demo.Entities.User;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repo.jpa.AuthRepo;
import com.example.demo.Repo.jpa.RefreshTokenRepository;
import com.example.demo.Services.MailService.EmailService;
import com.example.demo.Services.MailService.OtpService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthServiceInterface {

    private final OtpService otpService;
    private final EmailService emailService;
    private final AuthRepo authRepo;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProfileService profileService;

    @Override
    public ApiResponse<String> signup(SignUpReq signUpReq) {
        if (authRepo.existsByUsername(signUpReq.getUsername())) {
            throw new ApiExceptions("Username already exists. Please try a different username.");
        }

        if (authRepo.existsByEmail(signUpReq.getEmail())) {
            log.error("Account with email {} already exists", signUpReq.getEmail());
            throw new ApiExceptions("Account with this email already exists.");
        }

        try {
            String otp = otpService.generateOtp();
            otpService.saveOTP(signUpReq.getEmail(), otp);
            emailService.sendOtpEmail(signUpReq.getEmail(), otp);
            log.info("OTP sent to email: {}", signUpReq.getEmail());
            return ApiResponse.success("OTP sent to your email.");
        } catch (Exception e) {
            log.error("Error sending OTP to email {}: {}", signUpReq.getEmail(), e.getMessage());
            throw new ApiExceptions("Signup failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> verifyOtpAndSaveUser(String email, String otp, SignUpReq signUpReq) {
        if (!otpService.verifyOTP(email, otp)) {
            throw new ApiExceptions("Invalid OTP.");
        }

        if (authRepo.existsByUsername(signUpReq.getUsername())) {
            String suggested = signUpReq.getUsername() + "_" + (System.currentTimeMillis() % 1000);
            throw new ApiExceptions("Username '" + signUpReq.getUsername() + "' is taken. Try: " + suggested);
        }

        try {
            User user = new User();
            user.setEmail(signUpReq.getEmail());
            user.setUsername(signUpReq.getUsername());
            user.setPassword(passwordEncoder.encode(signUpReq.getPassword()));
            User savedUser = authRepo.save(user);

            // Create profile for the verified user
            profileService.createProfile(savedUser, savedUser.getUsername());

            log.info("User registered successfully with email: {}", email);
            return ApiResponse.success("User registered successfully. Please login to continue.");
        } catch (Exception e) {
            log.error("User registration failed for email {}: {}", email, e.getMessage());
            throw new ApiExceptions("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<LoginResponseDto> login(LoginReq loginRequest, HttpServletResponse response) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            User user = (User) authentication.getPrincipal();

            // Generate access token
            String accessToken = jwtService.generateAccessToken(user);

            // Create cookie for token
            ResponseCookie jwtCookie = ResponseCookie.from("token", accessToken)
                    .httpOnly(true)
                    .secure(false)              // Change to true in production (HTTPS)
                    .path("/")
                    .maxAge(24 * 60 * 60)       // 1 day
                    .sameSite("Lax")
                    .build();

            ResponseCookie userIdCookie = ResponseCookie.from("userId", String.valueOf(user.getId()))
                    .httpOnly(false)          // Allow frontend access
                    .secure(false)            // Set to true in production (HTTPS)
                    .path("/")
                    .maxAge(24 * 60 * 60)     // 1 day
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, userIdCookie.toString());  // User ID
            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            // Create and store refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            // Build response DTO
            LoginResponseDto responseDto = LoginResponseDto.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .build();

            log.info("Login successful for email: {}", loginRequest.getEmail());
            return ApiResponse.success(responseDto, "Login successful.");

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for email: {}", loginRequest.getEmail());
            throw new ApiExceptions("Invalid email or password.");
        } catch (Exception e) {
            log.error("Login failed for email {}: {}", loginRequest.getEmail(), e.getMessage());
            throw new ApiExceptions("Login failed: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<String> refreshAccessToken(String accessToken) {
        // If the current access token is still valid, just return it
        if (jwtService.validateToken(accessToken)) {
            return ApiResponse.success(accessToken, "Already valid");
        }

        // Extract user ID from expired access token
        Long userId = jwtService.getUserIdFromToken(accessToken);
        
        // Get the user
        User user = authRepo.findUserById(userId);
        if (user == null) {
            throw new ApiExceptions("User not found");
        }

        // Look up the refresh token using user
        RefreshToken getRefreshToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> new ApiExceptions("Login again"));

        // Validate the refresh token
        if (!jwtService.validateToken(getRefreshToken.getToken())) {
            throw new ApiExceptions("Refresh token expired or invalid");
        }
        
        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        return ApiResponse.success(newAccessToken, "New access token generated");
    }

    public String refreshAT(Long userId) {
        log.info("RefreshAT called for userId: {}", userId);
        
        User user = authRepo.findUserById(userId);
        if (user == null) {
            log.error("No user found for userId: {}", userId);
            throw new ResourceNotFoundException("No user found");
        }
        log.info("User found: {} ({})", user.getUsername(), user.getEmail());

        RefreshToken userRefreshToken = refreshTokenRepository.findByUser(user)
                .orElseThrow(() -> {
                    log.error("No refresh token found for userId: {}", userId);
                    return new ResourceNotFoundException("No refresh token info");
                });
        log.info("Refresh token found for userId: {}", userId);

        boolean verify = refreshTokenService.verifyExpiration(userRefreshToken);
        log.info("Refresh token verification result for userId {}: {}", userId, verify);

        if (!verify) {
            log.warn("Refresh token expired for userId: {}", userId);
            return "REFRESH_EXPIRED";
        }

        try {
            String newAccessToken = jwtService.generateAccess(
                    userRefreshToken.getToken(),
                    userId,
                    user.getUsername(),
                    user.getEmail()
            );
            log.info("Successfully generated new access token for userId: {}", userId);
            return newAccessToken;
        } catch (Exception e) {
            log.error("Error generating access token for userId {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
}

