package com.dockeriq.service.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "shipments")
public class Shipment {
    
    @Id
    private String id;
    
    private String trackingNumber;
    
    private BasicInformation basicInformation;
    private Map<String, String> customerFields;
    private List<String> imageIds; // GridFS file IDs
    private String notes;
    private String deviceInformation;
    
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional metadata
    private String createdBy;
    private String lastModifiedBy;
    private List<String> tags;
}
