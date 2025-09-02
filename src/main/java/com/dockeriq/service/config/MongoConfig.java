package com.dockeriq.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.LoggingEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;

@Configuration
public class MongoConfig {
    
    /**
     * Disable MongoDB query logging to prevent sensitive data exposure
     */
    @Bean
    public LoggingEventListener mongoLoggingEventListener() {
        return new LoggingEventListener() {
            @Override
            public void onBeforeConvert(BeforeConvertEvent<Object> event) {
                // Disable conversion logging
            }
            
            @Override
            public void onAfterLoad(AfterLoadEvent<Object> event) {
                // Disable load logging
            }
            
            @Override
            public void onAfterConvert(AfterConvertEvent<Object> event) {
                // Disable convert logging
            }
        };
    }
}

