package com.dockeriq.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class BasicInformation {

    @NotBlank(message = "Shipment title is required")
    @Size(max = 100, message = "Shipment title cannot exceed 100 characters")
    private String shipmentTitle;
    
    @NotBlank(message = "Destination is required")
    @Size(max = 200, message = "Destination cannot exceed 200 characters")
    private String destination;
    
    private String barcode;
    private String origin;
    private String carrier;
    private String trackingNumber;
    private Double weight;
    private String weightUnit;
    private Double dimensions;
    private String dimensionUnit;
    private String priority;
    private String estimatedDeliveryDate;
}
