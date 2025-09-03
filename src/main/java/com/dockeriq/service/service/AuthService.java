package com.dockeriq.service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dockeriq.service.dto.AuthRequest;
import com.dockeriq.service.dto.AuthResponse;
import com.dockeriq.service.model.User;
import com.dockeriq.service.model.UserDetails;
import com.dockeriq.service.repository.UserDetailsRepository;
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
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil = new JwtUtil();

    public AuthResponse authenticate(AuthRequest authRequest) {
        log.info("Authentication attempt for user: {}", authRequest.getEmail());
        
        // Find user by email only
        Optional<User> user = userRepository.findByEmail(authRequest.getEmail());
        
        if (user.isPresent()) {
            log.debug("User found with email: {}", authRequest.getEmail());
            
            if (passwordEncoder.matches(authRequest.getPassword(), user.get().getPassword())) {
                log.debug("Password validation successful for user: {}", authRequest.getEmail());
                
                Optional<UserDetails> userDetails = userDetailsRepository.findByEmail(user.get().getEmail());
                AuthResponse authResponse = new AuthResponse();
                
                String token = jwtUtil.generateToken(user.get().getEmail());
                authResponse.setToken(token);
                authResponse.setEmail(user.get().getEmail());
                authResponse.setRole(user.get().getRole());
                
                if (userDetails.isPresent()) {
                    log.debug("User details found for: {}", authRequest.getEmail());
                    authResponse.setFirstName(userDetails.get().getFirstName());
                    authResponse.setLastName(userDetails.get().getLastName());
                } else {
                    log.debug("No user details found for: {}", authRequest.getEmail());
                }
                
                log.info("Authentication successful for user: {} with role: {}", 
                        authRequest.getEmail(), user.get().getRole());
                return authResponse;
            } else {
                log.warn("Password validation failed for user: {}", authRequest.getEmail());
                throw new RuntimeException("Invalid email or password");
            }
        } else {
            log.warn("User not found with email: {}", authRequest.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
    }
    
    public void encodePassword(User user) {
        log.debug("Encoding password for user: {}", user.getEmail());
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            log.debug("Password needs encoding for user: {}", user.getEmail());
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            log.debug("Password encoded successfully for user: {}", user.getEmail());
        } else {
            log.debug("Password already encoded for user: {}", user.getEmail());
        }
    }
}
