package com.cogito.minijira.controller;

import com.cogito.minijira.repository.CommentRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/comments/admin")
@Profile("dev")
public class AdminController {

    private final CommentRepository repository;

    public AdminController(CommentRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/clear-db")
    @Transactional
    public ResponseEntity<Void> clearDb() {
        repository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
