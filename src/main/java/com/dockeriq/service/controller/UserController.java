package com.dockeriq.service.controller;

import com.dockeriq.service.model.User;
import com.dockeriq.service.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
        log.info("UserController initialized with UserService");
    }
    
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        log.info("Retrieving all users");
        try {
            List<User> users = userService.getAllUsers();
            log.debug("Retrieved {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Failed to retrieve users. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve users");
        }
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        try {
            User createdUser = userService.createUser(user);
            log.info("Successfully created user with email: {}", createdUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("user created successfully");
        } catch (RuntimeException e) {
            log.warn("User creation failed for email: {}. Error: {}", user.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                .body("User creation failed");
        }
    }
    
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        log.info("Retrieving user by email: {}", email);
        try {
            Optional<User> user = userService.getUserByEmail(email);
            if (user.isPresent()) {
                log.debug("User found with email: {}", email);
                return ResponseEntity.ok(user.get());
            } else {
                log.warn("User not found with email: {}", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + email);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve user with email: {}. Error: {}", email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve user");
        }
    }
    
    @PutMapping("/{email}")
    public ResponseEntity<?> updateUser(@PathVariable String email, @Valid @RequestBody User user) {
        log.info("Updating user with email: {}", email);
        try {
            User updatedUser = userService.updateUser(email, user);
            log.info("Successfully updated user with email: {}", email);
            return ResponseEntity.status(HttpStatus.OK)
                .body(updatedUser);
        } catch (RuntimeException e) {
            log.warn("User not found for update. Email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("User not found with email: " + email);
        }
    }
}

