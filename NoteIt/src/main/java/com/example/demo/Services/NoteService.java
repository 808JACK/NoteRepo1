package com.example.demo.Services;

import com.example.demo.Entities.Note;
import com.example.demo.Dtos.NoteDto;
import com.example.demo.Repo.mongo.NoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private ModelMapper modelMapper;
    
    // Create a new note
    public NoteDto createNote(NoteDto noteDto) {
        Note note = modelMapper.map(noteDto, Note.class);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        
        Note savedNote = noteRepository.save(note);
        return modelMapper.map(savedNote, NoteDto.class);
    }
    
    // Get all notes for a user
    public List<NoteDto> getAllNotesByUserId(String userId) {
        List<Note> notes = noteRepository.findByUserId(userId);
        return notes.stream()
                .map(note -> modelMapper.map(note, NoteDto.class))
                .collect(Collectors.toList());
    }
    
    // Get note by ID
    public Optional<NoteDto> getNoteById(String id) {
        Optional<Note> note = noteRepository.findById(id);
        return note.map(n -> modelMapper.map(n, NoteDto.class));
    }
    
    // Update note
    public NoteDto updateNote(String id, NoteDto noteDto) {
        Optional<Note> existingNote = noteRepository.findById(id);
        
        if (existingNote.isPresent()) {
            Note note = existingNote.get();
            note.setTitle(noteDto.getTitle());
            note.setContent(noteDto.getContent());
            note.setUpdatedAt(LocalDateTime.now());
            
            Note updatedNote = noteRepository.save(note);
            return modelMapper.map(updatedNote, NoteDto.class);
        }
        
        throw new RuntimeException("Note not found with id: " + id);
    }
    
    // Delete note
    public void deleteNote(String id) {
        if (noteRepository.existsById(id)) {
            noteRepository.deleteById(id);
        } else {
            throw new RuntimeException("Note not found with id: " + id);
        }
    }
    
    // Search notes
    public List<NoteDto> searchNotes(String userId, String searchTerm) {
        List<Note> notes = noteRepository.findByUserIdAndTitleOrContentContaining(userId, searchTerm);
        return notes.stream()
                .map(note -> modelMapper.map(note, NoteDto.class))
                .collect(Collectors.toList());
    }
}