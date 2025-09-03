package com.dockeriq.service.service;

import com.dockeriq.service.model.User;
import com.dockeriq.service.model.UserDetails;
import com.dockeriq.service.repository.UserDetailsRepository;
import com.dockeriq.service.repository.UserRepository;
import com.dockeriq.service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public User createUser(User user) {
      
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }
        
        // Encode password before saving
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public UserDetails updateUser(String email, UserDetails userDetails) {
        return userDetailsRepository.findByEmail(email)
                .map(existingDetails -> {
                    existingDetails.setFirstName(userDetails.getFirstName());
                    existingDetails.setLastName(userDetails.getLastName());
                    existingDetails.setAddress(userDetails.getAddress());
                    existingDetails.setPhoneNumber(userDetails.getPhoneNumber());
                    existingDetails.setUpdatedAt(LocalDateTime.now());
                    return userDetailsRepository.save(existingDetails);
                })
                .orElseThrow(() -> new RuntimeException("User details not found for email: " + email));
    }
    
    public void deleteUser(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            userRepository.deleteById(user.get().getId());
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }
}

