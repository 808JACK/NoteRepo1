package com.example.demo.Services;

import com.example.demo.Entities.User;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.Repo.jpa.AuthRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class UserService {


    private final AuthRepo authRepo;
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = authRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
        return user;
    }
    public User getUserById(Long userId) {
        return authRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
    }

    public User getUserByEmail(String email) {
        return authRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found"));
    }

    public User getUserByUsername(String username) {
        return authRepo.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User with username " + username + " not found"));
    }
}
