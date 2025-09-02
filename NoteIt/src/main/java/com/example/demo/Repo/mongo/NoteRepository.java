package com.example.demo.Repo.mongo;

import com.example.demo.Entities.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends MongoRepository<Note, String> {
    
    List<Note> findByUserId(String userId);
    
    List<Note> findByUserIdAndIsArchived(String userId, boolean isArchived);
    
    List<Note> findByUserIdAndCategory(String userId, String category);
    
    @Query("{'userId': ?0, 'title': {$regex: ?1, $options: 'i'}}")
    List<Note> findByUserIdAndTitleContaining(String userId, String title);
    
    @Query("{'userId': ?0, '$or': [{'title': {$regex: ?1, $options: 'i'}}, {'content': {$regex: ?1, $options: 'i'}}]}")
    List<Note> findByUserIdAndTitleOrContentContaining(String userId, String searchTerm);
    
    long countByUserId(String userId);
}