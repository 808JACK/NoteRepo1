package com.example.demo.Services;

import com.example.demo.Dtos.ProfileDTO;
import com.example.demo.Entities.Profile;
import com.example.demo.Entities.User;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repo.jpa.ProfileRepo;
import com.example.demo.Repo.jpa.AuthRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepo profileRepo;
    private final AuthRepo authRepo;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public ProfileDTO getProfile(Long userId) {
        Profile profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return convertToDTO(profile);
    }

    public ProfileDTO updateProfile(Long userId, ProfileDTO profileDTO) {
        Profile profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        profile.setDisplayName(profileDTO.getDisplayName());
        profile.setBio(profileDTO.getBio());
        profile.setCustomStatus(profileDTO.getCustomStatus());
        if (profileDTO.getAvatarUrl() != null) {
            profile.setAvatarUrl(profileDTO.getAvatarUrl());
        }

        Profile updatedProfile = profileRepo.save(profile);
        return convertToDTO(updatedProfile);
    }

    public String uploadAvatar(Long userId, MultipartFile file) {
        Profile profile = profileRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        try {
            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename).normalize();

            // Ensure the file is within the upload directory (security check)
            if (!filePath.getParent().equals(uploadPath)) {
                throw new RuntimeException("Cannot store file outside upload directory.");
            }

            // Save file with REPLACE_EXISTING to handle cases where file might exist
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Update profile with new avatar URL - store only the filename part
            String avatarUrl = "/uploads/" + filename;
            profile.setAvatarUrl(avatarUrl);
            profileRepo.save(profile);

            return avatarUrl;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public Profile createProfile(User user, String displayName) {
        Profile profile = new Profile();
        profile.setUser(user);
        profile.setDisplayName(displayName);
        return profileRepo.save(profile);
    }

    public List<ProfileDTO> getProfilesByUserIds(List<Long> userIds) {
        List<Profile> profiles = profileRepo.findAllByUser_IdIn(userIds);
        return profiles.stream().map(this::convertToDTO).toList();
    }

    private ProfileDTO convertToDTO(Profile profile) {
        return ProfileDTO.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .displayName(profile.getDisplayName())
                .avatarUrl(profile.getAvatarUrl())
                .bio(profile.getBio())
                .customStatus(profile.getCustomStatus())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
} 