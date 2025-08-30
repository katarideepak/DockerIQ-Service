package com.dockeriq.service.config;

import com.dockeriq.service.model.User;
import com.dockeriq.service.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    
    @Autowired
    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        // Only initialize if no users exist
        if (userRepository.count() == 0) {
            User superUser = new User();
            superUser.setEmail("katarideepak@gmail.com");
            superUser.setPassword("hello123"); // remove later
            superUser.setFirstName("deepak");
            superUser.setLastName("k");
            superUser.setRole("supervisor");
            superUser.setActive(true);
            superUser.setPasswordReset(false);
            
            userRepository.save(superUser);
            
            log.info("super user created");
        }
    }
}

