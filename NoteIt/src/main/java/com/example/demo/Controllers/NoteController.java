package com.example.demo.Controllers;

import com.example.demo.Dtos.NoteDto;
import com.example.demo.Services.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "*")
public class NoteController {
    
    @Autowired
    private NoteService noteService;
    
    // Create a new note
    @PostMapping
    public ResponseEntity<NoteDto> createNote(@Valid @RequestBody NoteDto noteDto) {
        try {
            NoteDto createdNote = noteService.createNote(noteDto);
            return new ResponseEntity<>(createdNote, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get all notes for a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteDto>> getAllNotesByUserId(@PathVariable String userId) {
        try {
            List<NoteDto> notes = noteService.getAllNotesByUserId(userId);
            return new ResponseEntity<>(notes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Get note by ID
    @GetMapping("/{id}")
    public ResponseEntity<NoteDto> getNoteById(@PathVariable String id) {
        try {
            Optional<NoteDto> note = noteService.getNoteById(id);
            if (note.isPresent()) {
                return new ResponseEntity<>(note.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Update note
    @PutMapping("/{id}")
    public ResponseEntity<NoteDto> updateNote(@PathVariable String id, @Valid @RequestBody NoteDto noteDto) {
        try {
            NoteDto updatedNote = noteService.updateNote(id, noteDto);
            return new ResponseEntity<>(updatedNote, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Delete note
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNote(@PathVariable String id) {
        try {
            noteService.deleteNote(id);
            return new ResponseEntity<>("Note deleted successfully", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Note not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting note", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // Search notes
    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<NoteDto>> searchNotes(@PathVariable String userId, @RequestParam String q) {
        try {
            List<NoteDto> notes = noteService.searchNotes(userId, q);
            return new ResponseEntity<>(notes, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}