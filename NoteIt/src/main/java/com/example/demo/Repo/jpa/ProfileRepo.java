package com.example.demo.Repo.jpa;

import com.example.demo.Entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepo extends JpaRepository<Profile, Long> {
    
    Optional<Profile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    List<Profile> findAllByUser_IdIn(List<Long> userIds);
}