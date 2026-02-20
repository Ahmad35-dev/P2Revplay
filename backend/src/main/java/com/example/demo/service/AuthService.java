package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

// --- Added Log4j2 Imports ---
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    // --- Initialized the Logger ---
    private static final Logger logger = LogManager.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public String registerUser(RegisterRequest request) {
        logger.info("Attempting to register new user with email: {}", request.getEmail());

        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed: Email {} is already in use", request.getEmail());
            throw new RuntimeException("Error: Email is already in use!");
        }

        // 2. Create the new User entity
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Hash the password before saving!
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Ensure role is formatted correctly (e.g., "USER" or "ARTIST")
        String role = (request.getRole() != null) ? request.getRole().toUpperCase() : "USER";
        user.setRole(role);

        // 3. Save to database
        userRepository.save(user);
        logger.info("User registered successfully with ID: {} and Role: {}", user.getUserId(), user.getRole());

        return "User registered successfully!";
    }

    public Map<String, String> loginUser(LoginRequest request) {
        logger.debug("Authentication attempt for email: {}", request.getEmail());

        try {
            // 1. Authenticate the user (Checks if email and password match)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            logger.info("Authentication successful for email: {}", request.getEmail());
        } catch (Exception e) {
            logger.warn("Authentication failed for email: {}. Reason: Invalid credentials", request.getEmail());
            throw e; // Rethrow so the controller catches it
        }

        // 2. Fetch user to get their role
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.error("User not found in DB after successful authentication: {}", request.getEmail());
                    return new RuntimeException("User not found");
                });

        // 3. Generate JWT Token
        String token = jwtUtil.generateToken(user.getEmail(), "ROLE_" + user.getRole());
        logger.debug("JWT Token generated successfully for user: {}", user.getEmail());

        // 4. Return token in a Map (which Spring converts to JSON)
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole());
        response.put("name", user.getName());

        return response;
    }
}