package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tasks/{taskId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getByTaskId(taskId));
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@PathVariable Long taskId,
                                                 @RequestBody Map<String, String> body,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        String text = body.get("text");
        // TODO: Resolve userId from AuthService via API call
        Long userId = 1L; // Placeholder
        return ResponseEntity.ok(commentService.create(taskId, text, userId));
    }
}
