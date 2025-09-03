package com.dockeriq.service.service;

import com.dockeriq.service.model.User;
import com.dockeriq.service.model.UserDetails;
import com.dockeriq.service.repository.UserDetailsRepository;
import com.dockeriq.service.repository.UserRepository;
import com.dockeriq.service.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;
    
    public List<User> getAllUsers() {
        log.debug("Retrieving all users from database");
        List<User> users = userRepository.findAll();
        log.debug("Retrieved {} users from database", users.size());
        return users;
    }

    public Optional<User> getUserByEmail(String email) {
        log.debug("Retrieving user by email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            log.debug("User found with email: {}", email);
        } else {
            log.debug("User not found with email: {}", email);
        }
        return user;
    }
    
    public User createUser(User user) {
        log.info("Creating new user with email: {}", user.getEmail());
        
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("User creation failed - email already exists: {}", user.getEmail());
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        log.debug("Email is unique, proceeding with user creation");
        
        // Encode password before saving
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        log.info("Successfully created user with email: {} and ID: {}", 
                savedUser.getEmail(), savedUser.getId());
        return savedUser;
    }
    
    public UserDetails updateUser(String email, UserDetails userDetails) {
        log.info("Updating user details for email: {}", email);
        
        return userDetailsRepository.findByEmail(email)
                .map(existingDetails -> {
                    log.debug("Found existing user details for email: {}", email);
                    existingDetails.setFirstName(userDetails.getFirstName());
                    existingDetails.setLastName(userDetails.getLastName());
                    existingDetails.setAddress(userDetails.getAddress());
                    existingDetails.setPhoneNumber(userDetails.getPhoneNumber());
                    existingDetails.setUpdatedAt(LocalDateTime.now());
                    
                    UserDetails updatedDetails = userDetailsRepository.save(existingDetails);
                    log.info("Successfully updated user details for email: {}", email);
                    return updatedDetails;
                })
                .orElseThrow(() -> {
                    log.warn("User details not found for update. Email: {}", email);
                    return new RuntimeException("User details not found for email: " + email);
                });
    }
    
    public void deleteUser(String email) {
        log.info("Deleting user with email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            log.debug("Found user for deletion with email: {}", email);
            userRepository.deleteById(user.get().getId());
            log.info("Successfully deleted user with email: {}", email);
        } else {
            log.warn("User not found for deletion. Email: {}", email);
            throw new RuntimeException("User not found with email: " + email);
        }
    }
}

