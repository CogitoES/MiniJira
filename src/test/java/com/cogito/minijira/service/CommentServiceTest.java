package com.cogito.minijira.service;

import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.domain.Task;
import com.cogito.minijira.domain.User;
import com.cogito.minijira.repository.CommentRepository;
import com.cogito.minijira.repository.TaskRepository;
import com.cogito.minijira.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void createComment_ShouldSaveComment() {
        Task task = new Task();
        User user = new User();
        Comment comment = new Comment();
        
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        Comment created = commentService.create(1L, "text", "test@test.com");

        assertNotNull(created);
        verify(commentRepository).save(any(Comment.class));
    }
}
