package com.dockeriq.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {
    
    @GetMapping
    public Map<String, Object> health() {
        log.debug("Health check request received");
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "DockerIQ Service");
        health.put("version", "1.0.0");
        log.debug("Health check response: {}", health);
        return health;
    }
}

