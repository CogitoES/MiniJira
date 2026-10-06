package com.cogito.minijira.controller;

import com.cogito.minijira.client.AuthClient;
import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.repository.CommentRepository;
import com.cogito.minijira.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Comment Controller - REST endpoints for comment management
 * 
 * Provides endpoints for creating, retrieving, updating, and deleting comments
 * on tasks. All endpoints require authentication.
 */
@RestController
@RequestMapping("/tasks")
public class CommentController {

    // ========== Dependencies ==========

    private final CommentRepository commentRepository;
    private final AuthClient authClient;

    /**
     * Constructs a CommentController with required dependencies
     * 
     * @param commentRepository the comment repository
     * @param authClient the Feign client for auth service communication
     */
    public CommentController(CommentRepository commentRepository, AuthClient authClient) {
        this.commentRepository = commentRepository;
        this.authClient = authClient;
    }

    // ========== Private Methods ==========

    /**
     * Validates that a user exists in the auth service
     * 
     * @param userId the user ID to validate
     * @throws org.springframework.web.server.ResponseStatusException if user does not exist
     */
    private void validateUserExists(Long userId) {
        try {
            Boolean exists = authClient.exists(userId);
            if (exists == null || !exists) {
                throw new RuntimeException("User does not exist");
            }
        } catch (Exception e) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user session");
        }
    }

    // ========== REST Endpoints ==========

    /**
     * Retrieves all comments for a specific task
     * 
     * @param taskId the task ID
     * @return list of comments in chronological order
     */
    @GetMapping("/{taskId}/comments")
    public ResponseEntity<List<Comment>> getCommentsByTaskId(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId));
    }

    /**
     * Creates a new comment on a task
     * 
     * If a comment with the same Jira key exists, it will be updated instead.
     * 
     * @param taskId the task ID to add the comment to
     * @param comment the comment data
     * @return the created or updated comment
     */
    @PostMapping("/{taskId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable Long taskId, @RequestBody Comment comment) {
        Comment existingComment = null;
        if (comment.getJiraKey() != null) {
            existingComment = commentRepository.findByJiraKey(comment.getJiraKey()).orElse(null);
        }

        if (existingComment != null) {
            existingComment.setJiraKey(comment.getJiraKey());
            existingComment.setText(comment.getText());
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

    /**
     * Retrieves a specific comment by ID
     * 
     * @param commentId the comment ID
     * @return the comment details, or 404 if not found
     */
    @GetMapping("/comments/{commentId}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long commentId) {
        return commentRepository.findById(commentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing comment
     * 
     * @param commentId the comment ID to update
     * @param commentDetails the updated comment data
     * @return the updated comment, or 404 if not found
     */
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<Comment> updateComment(@PathVariable Long commentId, @RequestBody Comment commentDetails) {
        return commentRepository.findById(commentId)
                .map(comment -> {
                    comment.setText(commentDetails.getText());
                    return ResponseEntity.ok(commentRepository.save(comment));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a comment
     * 
     * @param commentId the comment ID to delete
     * @return no content response, or 404 if not found
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            return ResponseEntity.notFound().build();
        }
        commentRepository.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }
}
