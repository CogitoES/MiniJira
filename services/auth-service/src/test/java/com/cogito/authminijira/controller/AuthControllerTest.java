package com.cogito.authminijira.controller;

import com.cogito.authminijira.service.AuthService;
import com.cogito.minijira.common.dto.AuthResponse;
import com.cogito.minijira.common.dto.LoginRequest;
import com.cogito.minijira.common.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_ShouldReturnOk() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");

        ResponseEntity<String> response = authController.register(request);

        assertEquals(200, response.getStatusCode().value());
        verify(authService, times(1)).register(request);
    }

    @Test
    void login_ShouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        AuthResponse expectedResponse = new AuthResponse();
        when(authService.login(request)).thenReturn(expectedResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expectedResponse, response.getBody());
        verify(authService, times(1)).login(request);
    }

    @Test
    void refresh_ShouldReturnToken() {
        String token = "oldToken";
        String newToken = "newToken";
        when(authService.refreshToken(token)).thenReturn(newToken);

        ResponseEntity<String> response = authController.refresh(token);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(newToken, response.getBody());
        verify(authService, times(1)).refreshToken(token);
    }
}
