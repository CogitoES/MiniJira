package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.repository.CommentRepository;
import com.cogito.minijira.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class CommentController {

    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId));
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable Long taskId, @RequestBody Comment comment) {
        comment.setTaskId(taskId);

        // Resolve userId directly from SecurityContext
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        comment.setUserId(principal.getUserId());

        return ResponseEntity.ok(commentRepository.save(comment));
    }
}
