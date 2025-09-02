package com.dockeriq.service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dockeriq.service.dto.AuthRequest;
import com.dockeriq.service.dto.AuthResponse;
import com.dockeriq.service.model.User;
import com.dockeriq.service.repository.UserRepository;
import com.dockeriq.service.security.JwtUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil = new JwtUtil();

        public AuthResponse authenticate(AuthRequest authRequest) {
        // Find user by email only
        Optional<User> user = userRepository.findByEmail(authRequest.getEmail());
        
        if (user.isPresent() && passwordEncoder.matches(authRequest.getPassword(), user.get().getPassword())) {
            AuthResponse authResponse = new AuthResponse();
            authResponse.setToken(jwtUtil.generateToken(user.get().getEmail()));
            authResponse.setEmail(user.get().getEmail());
            authResponse.setRole(user.get().getRole());
            authResponse.setFirstName(user.get().getFirstName());
            authResponse.setLastName(user.get().getLastName());
            return authResponse;
        } else {
            log.warn("Authentication failed for user: {}", authRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
    }
    
    public void encodePassword(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }
}
