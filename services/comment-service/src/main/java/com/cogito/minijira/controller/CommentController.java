package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.repository.CommentRepository;
import com.cogito.minijira.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class CommentController {

    private final CommentRepository commentRepository;
    private final UserService userService;

    public CommentController(CommentRepository commentRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId));
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable Long taskId, @RequestBody Comment comment) {
        comment.setTaskId(taskId);

        // Resolve userId from SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        comment.setUserId(userService.getUserIdByUsername(username));

        return ResponseEntity.ok(commentRepository.save(comment));
    }
}
