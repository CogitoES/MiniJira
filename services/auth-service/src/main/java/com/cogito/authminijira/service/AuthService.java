package com.cogito.authminijira.service;

import com.cogito.authminijira.domain.User;
import com.cogito.minijira.common.dto.AuthResponse;
import com.cogito.minijira.common.dto.LoginRequest;
import com.cogito.minijira.common.dto.RegisterRequest;
import com.cogito.authminijira.repository.UserRepository;
import com.cogito.authminijira.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * AuthService - Authentication and Authorization Service
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email cannot be empty");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setEncryptedPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getEncryptedPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail(), user.getId());
        
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        
        return new AuthResponse(token, refreshToken);
    }

    /**
     * Refreshes an expired access token using a valid refresh token
     * 
     * Validates refresh token is valid and matches stored token for user.
     * @param refreshToken The refresh token provided by client
     * @return New access token
     * @throws RuntimeException if refresh token is invalid or doesn't match stored token
     */
    public String refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String email = jwtTokenProvider.getUsernameFromToken(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Refresh token does not match");
        }
        
        return jwtTokenProvider.generateToken(email, user.getId());
    }

    /**
     * Checks whether a user exists by their ID
     * @param id The user ID to check
     * @return true if user exists, false otherwise
     */
    public boolean exists(Long id) {
        return userRepository.existsById(id);
    }
}
