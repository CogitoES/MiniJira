package com.cogito.minijira.service;

import com.cogito.minijira.domain.User;
import com.cogito.minijira.common.dto.RegisterRequest;
import com.cogito.minijira.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setEncryptedPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // Default role
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findAll().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void save(User user) {
        userRepository.save(user);
    }
}
