package com.cogito.minijira.controller;

import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.repository.CommentRepository;
import com.cogito.minijira.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CommentControllerTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCommentsByTaskId_ShouldReturnComments() {
        Long taskId = 1L;
        Comment comment = new Comment();
        when(commentRepository.findByTaskIdOrderByCreatedAtAsc(taskId)).thenReturn(Collections.singletonList(comment));

        ResponseEntity<List<Comment>> response = commentController.getCommentsByTaskId(taskId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(commentRepository, times(1)).findByTaskIdOrderByCreatedAtAsc(taskId);
    }

    @Test
    void createComment_ShouldReturnCreatedComment() {
        Long taskId = 1L;
        Comment comment = new Comment();
        String username = "testUser";
        Long userId = 100L;

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserIdByUsername(username)).thenReturn(userId);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        ResponseEntity<Comment> response = commentController.createComment(taskId, comment);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(userId, comment.getUserId());
        verify(commentRepository, times(1)).save(comment);
    }
}
