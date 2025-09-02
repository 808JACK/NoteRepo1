package com.example.demo.Controllers;

import com.example.demo.Dtos.ApiResponse;
import com.example.demo.Dtos.ProfileDTO;
import com.example.demo.Entities.Profile;
import com.example.demo.Services.ProfileService;
import com.example.demo.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth/profiles")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;
    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getProfile(@PathVariable Long userId) {
        ProfileDTO profile = profileService.getProfile(userId);
        return ResponseEntity.ok(new ApiResponse(true, "Profile fetched successfully", profile));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse> updateProfile(
            @PathVariable Long userId,
            @RequestBody ProfileDTO profileDTO
    ) {
        ProfileDTO updatedProfile = profileService.updateProfile(userId, profileDTO);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully", updatedProfile));
    }

    @PostMapping("/{userId}/avatar")
    public ResponseEntity<ApiResponse> uploadAvatar(
            @PathVariable Long userId,
            @RequestParam("avatar") MultipartFile file
    ) {
        String avatarUrl = profileService.uploadAvatar(userId, file);
        return ResponseEntity.ok(new ApiResponse(true, "Avatar uploaded successfully", 
            new ProfileDTO().builder()
                .userId(userId)
                .avatarUrl(avatarUrl)
                .build()
        ));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse> getProfilesBatch(@RequestBody Map<String, List<Long>> request) {
        List<Long> userIds = request.get("userIds");
        List<ProfileDTO> profiles = profileService.getProfilesByUserIds(userIds);
        return ResponseEntity.ok(new ApiResponse(true, "Profiles fetched successfully", profiles));
    }

} 