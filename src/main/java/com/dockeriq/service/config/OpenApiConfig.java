package com.dockeriq.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DockerIQ Service API")
                        .version("1.0.0")
                        .description("API documentation for DockerIQ Service")
                        .contact(new Contact()
                                .name("DockerIQ Team")
                                .email("support@dockeriq.com")));
    }
}
