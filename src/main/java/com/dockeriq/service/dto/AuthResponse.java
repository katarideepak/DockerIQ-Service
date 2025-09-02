package com.dockeriq.service.dto;

import lombok.Data;

@Data
public class AuthResponse {

    private String token;
    private String email;
    private String role;
    private String firstName;
    private String lastName;
}
