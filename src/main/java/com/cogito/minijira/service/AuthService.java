package com.cogito.minijira.service;

import com.cogito.minijira.domain.User;
import com.cogito.minijira.dto.AuthResponse;
import com.cogito.minijira.dto.LoginRequest;
import com.cogito.minijira.dto.RegisterRequest;
import com.cogito.minijira.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        userService.registerUser(request);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());
        
        if (!passwordEncoder.matches(request.getPassword(), user.getEncryptedPassword())) {
            throw new RuntimeException("Invalid password");
        }
        String token = jwtTokenProvider.generateToken(user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        
        user.setRefreshToken(refreshToken);
        userService.save(user);
        
        return new AuthResponse(token, refreshToken);
    }

    public String refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userService.findByEmail(email);
        
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh token does not match");
        }
        
        return jwtTokenProvider.generateToken(email);
    }
}
