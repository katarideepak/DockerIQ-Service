package com.dockeriq.service.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class AddShipment {
    
    @NotNull(message = "Basic information is required")
    @Valid
    private BasicInformation basicInformation;
    
    private Map<String, String> customerFields;
    
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageIds; // GridFS file IDs
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    @Size(max = 500, message = "Device information cannot exceed 500 characters")
    private String deviceInformation;
    
    @NotNull(message = "Created by is required")
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}