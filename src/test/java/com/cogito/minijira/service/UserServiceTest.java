package com.cogito.minijira.service;

import com.cogito.minijira.domain.User;
import com.cogito.minijira.common.dto.RegisterRequest;
import com.cogito.minijira.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
    }

    @Test
    void registerUser_ShouldSaveUser() {
        User user = new User();
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.registerUser(registerRequest);

        assertNotNull(savedUser);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password");
    }

    @Test
    void findByEmail_UserFound_ShouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.findAll()).thenReturn(java.util.List.of(user));

        User foundUser = userService.findByEmail("test@example.com");

        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void findByEmail_UserNotFound_ShouldThrowException() {
        when(userRepository.findAll()).thenReturn(java.util.List.of());

        assertThrows(RuntimeException.class, () -> userService.findByEmail("nonexistent@example.com"));
    }
}
