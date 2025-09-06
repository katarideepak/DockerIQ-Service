package com.dockeriq.service.config;

import com.dockeriq.service.model.User;
import com.dockeriq.service.repository.UserRepository;
import com.dockeriq.service.service.AuthService;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;
    
    @Transactional
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no users exist
        if (userRepository.count() == 0) {
            User superUser = new User();
            superUser.setEmail("katarideepak@gmail.com");
            superUser.setPassword("hello123"); // remove later
            superUser.setCreatedBy("admin");
            superUser.setRole("supervisor");
            superUser.setActive(true);
            superUser.setPasswordReset(false);
            superUser.setCreatedAt(LocalDateTime.now());
            superUser.setUpdatedAt(LocalDateTime.now());

            superUser.setFirstName("deepak");
            superUser.setLastName("k");
            superUser.setAddress("123 Main St");
            superUser.setPhoneNumber("9988998899");
            superUser.setEmail(superUser.getEmail());
            superUser.setCreatedAt(LocalDateTime.now());
            superUser.setUpdatedAt(LocalDateTime.now());
           
            // Encode password before saving
            authService.encodePassword(superUser);
            userRepository.save(superUser);
            log.info("super user created");
        }
    }
}

