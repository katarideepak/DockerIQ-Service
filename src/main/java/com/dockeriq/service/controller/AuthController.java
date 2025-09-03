package com.dockeriq.service.controller;

import com.dockeriq.service.dto.AuthRequest;
import com.dockeriq.service.dto.AuthResponse;
import com.dockeriq.service.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Login attempt for user: {}", authRequest.getEmail());
        try {
            AuthResponse response = authService.authenticate(authRequest);
            log.info("Login successful for user: {}", authRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Login failed for user: {}. Error: {}", authRequest.getEmail(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String token) {
        log.debug("Token validation request received");
        if (token != null && token.startsWith("Bearer ")) {
            log.debug("Token format is valid");
            return ResponseEntity.ok("Token is valid");
        }
        log.warn("Invalid token format provided");
        return ResponseEntity.badRequest().body("Invalid token");
    }
}
