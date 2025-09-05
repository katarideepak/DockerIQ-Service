package com.dockeriq.service.config;

import com.dockeriq.service.model.User;
import com.dockeriq.service.model.UserDetails;
import com.dockeriq.service.repository.UserDetailsRepository;
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
    private UserDetailsRepository userDetailsRepository;
    
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
            superUser.setRole("SUPERVISOR");
            superUser.setActive(true);
            superUser.setPasswordReset(false);
            superUser.setCreatedAt(LocalDateTime.now());
            superUser.setUpdatedAt(LocalDateTime.now());

            UserDetails userDetails = new UserDetails();
            userDetails.setFirstName("deepak");
            userDetails.setLastName("k");
            userDetails.setAddress("123 Main St");
            userDetails.setPhoneNumber("9988998899");
            userDetails.setEmail(superUser.getEmail());
            userDetails.setCreatedAt(LocalDateTime.now());
            userDetails.setUpdatedAt(LocalDateTime.now());
           
            // Encode password before saving
            authService.encodePassword(superUser);
            userRepository.save(superUser);
            userDetailsRepository.save(userDetails);
            log.info("super user created");
        }
    }
}

