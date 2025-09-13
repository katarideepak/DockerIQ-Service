package com.dockeriq.service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
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
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    @GetMapping
    public Map<String, Object> health() {
        log.debug("Health check request received");
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("service", "DockerIQ Service");
        health.put("version", "1.0.0");
        
        // Test database connection
        Map<String, Object> database = testDatabaseConnection();
        health.put("database", database);
        
        log.debug("Health check response: {}", health);
        return health;
    }
    
    @GetMapping("/db")
    public Map<String, Object> testDatabaseConnection() {
        log.info("Testing DocumentDB connection...");
        Map<String, Object> dbStatus = new HashMap<>();
        
        try {
            // Test basic connectivity
            String databaseName = mongoTemplate.getDb().getName();
            dbStatus.put("databaseName", databaseName);
            
            // Test a simple operation
            mongoTemplate.getCollection("test_connection").countDocuments();
            dbStatus.put("status", "CONNECTED");
            dbStatus.put("message", "DocumentDB connection successful");
            dbStatus.put("timestamp", LocalDateTime.now());
            
            log.info("DocumentDB connection test successful. Database: {}", databaseName);
            
        } catch (Exception e) {
            dbStatus.put("status", "DISCONNECTED");
            dbStatus.put("message", "DocumentDB connection failed: " + e.getMessage());
            dbStatus.put("timestamp", LocalDateTime.now());
            dbStatus.put("error", e.getClass().getSimpleName());
            
            log.error("DocumentDB connection test failed: {}", e.getMessage(), e);
        }
        
        return dbStatus;
    }
}

