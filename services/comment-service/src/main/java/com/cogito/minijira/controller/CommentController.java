package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.repository.CommentRepository;
import com.cogito.minijira.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class CommentController {

    private final CommentRepository commentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    private void validateUserExists(Long userId) {
        try {
            Boolean exists = restTemplate.getForObject("http://localhost:8081/auth/exists/" + userId, Boolean.class);
            if (exists == null || !exists) {
                throw new RuntimeException("User does not exist");
            }
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user session");
        }
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId));
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable Long taskId, @RequestBody Comment comment) {
        Comment existingComment = null;
        if (comment.getJiraKey() != null) {
            existingComment = commentRepository.findByJiraKey(comment.getJiraKey()).orElse(null);
        }
        if (existingComment == null) {
            existingComment = commentRepository.findByText(comment.getText()).orElse(null);
        }

        if (existingComment != null) {
            existingComment.setJiraKey(comment.getJiraKey());
            existingComment.setText(comment.getText());
            // Preserve task/user id
            comment = existingComment;
        } else {
            comment.setTaskId(taskId);
            
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isInternal = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_INTERNAL"));

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            Long userId = principal.getUserId();

            if (!isInternal) {
                validateUserExists(userId);
            }
            comment.setUserId(userId);
        }

        return ResponseEntity.ok(commentRepository.save(comment));
    }
}
