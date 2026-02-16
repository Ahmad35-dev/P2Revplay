package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

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
        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
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

        return "User registered successfully!";
    }

    public Map<String, String> loginUser(LoginRequest request) {
        // 1. Authenticate the user (Checks if email and password match)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Fetch user to get their role
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3. Generate JWT Token
        String token = jwtUtil.generateToken(user.getEmail(), "ROLE_" + user.getRole());

        // 4. Return token in a Map (which Spring converts to JSON)
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("role", user.getRole());
        response.put("name", user.getName());

        return response;
    }
}