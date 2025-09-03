package com.dockeriq.service.controller;

import com.dockeriq.service.model.AddShipment;
import com.dockeriq.service.model.Shipment;
import com.dockeriq.service.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/shipments")
@CrossOrigin(origins = "*")
public class ShipmentController {
    
    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public ShipmentController(ShipmentService shipmentService, ObjectMapper objectMapper) {
        this.shipmentService = shipmentService;
        this.objectMapper = objectMapper;
        log.info("ShipmentController initialized with ShipmentService and ObjectMapper");
    }
    
    /**
     * Create shipment with images using multipart form data
     * @param shipmentDataJson JSON string containing shipment data
     * @param images list of uploaded images (optional)
     * @return created shipment
     */
    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createShipmentWithImages(
            @RequestParam("shipmentData") String shipmentDataJson,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        
        log.info("Creating shipment with images. Images count: {}", images != null ? images.size() : 0);
        try {
            // Deserialize the JSON string to AddShipment object
            AddShipment addShipment = objectMapper.readValue(shipmentDataJson, AddShipment.class);
            log.debug("Deserialized shipment data: {}", addShipment.getBasicInformation());
            
            Shipment createdShipment = shipmentService.createShipmentWithImages(addShipment, images);
            log.info("Successfully created shipment with ID: {} and tracking number: {}", 
                    createdShipment.getId(), createdShipment.getTrackingNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdShipment);
        } catch (Exception e) {
            log.error("Failed to create shipment with images. Error: {}", e.getMessage(), e);
            if (e instanceof IllegalArgumentException) {
                return ResponseEntity.badRequest().body(e.getMessage());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create shipment: " + e.getMessage());
            }
        }
    }
    
    /**
     * Create shipment without images using JSON
     * @param addShipment shipment data
     * @return created shipment
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createShipment(@Valid @RequestBody AddShipment addShipment) {
        log.info("Creating shipment without images for user: {}", addShipment.getCreatedBy());
        try {
            Shipment createdShipment = shipmentService.createShipment(addShipment);
            log.info("Successfully created shipment with ID: {} and tracking number: {}", 
                    createdShipment.getId(), createdShipment.getTrackingNumber());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdShipment);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid shipment data provided: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create shipment. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to create shipment: " + e.getMessage());
        }
    }
    
    /**
     * Get shipment by ID
     * @param id shipment ID
     * @return shipment if found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getShipmentById(@PathVariable String id) {
        log.info("Retrieving shipment by ID: {}", id);
        try {
            Optional<Shipment> shipment = shipmentService.getShipmentById(id);
            if (shipment.isPresent()) {
                log.debug("Shipment found with ID: {}", id);
                return ResponseEntity.ok(shipment.get());
            } else {
                log.warn("Shipment not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Shipment not found with id: " + id);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve shipment with ID: {}. Error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve shipment");
        }
    }
    
    /**
     * Get shipment by tracking number
     * @param trackingNumber tracking number
     * @return shipment if found
     */
    @GetMapping("/tracking/{trackingNumber}")
    public ResponseEntity<?> getShipmentByTrackingNumber(@PathVariable String trackingNumber) {
        log.info("Retrieving shipment by tracking number: {}", trackingNumber);
        try {
            Optional<Shipment> shipment = shipmentService.getShipmentByTrackingNumber(trackingNumber);
            if (shipment.isPresent()) {
                log.debug("Shipment found with tracking number: {}", trackingNumber);
                return ResponseEntity.ok(shipment.get());
            } else {
                log.warn("Shipment not found with tracking number: {}", trackingNumber);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Shipment not found with tracking number: " + trackingNumber);
            }
        } catch (Exception e) {
            log.error("Failed to retrieve shipment with tracking number: {}. Error: {}", 
                    trackingNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve shipment");
        }
    }
    
    /**
     * Get all shipments
     * @return list of all shipments
     */
    @GetMapping
    public ResponseEntity<?> getAllShipments() {
        log.info("Retrieving all shipments");
        try {
            List<Shipment> shipments = shipmentService.getAllShipments();
            log.debug("Retrieved {} shipments", shipments.size());
            return ResponseEntity.ok(shipments);
        } catch (Exception e) {
            log.error("Failed to retrieve shipments. Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve shipments");
        }
    }
    
    /**
     * Update shipment status
     * @param id shipment ID
     * @param status new status
     * @return updated shipment
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateShipmentStatus(@PathVariable String id, @RequestBody String status) {
        log.info("Updating shipment status. ID: {}, New status: {}", id, status);
        try {
            Shipment updatedShipment = shipmentService.updateShipmentStatus(id, status, "system");
            log.info("Successfully updated shipment status. ID: {}, New status: {}", id, status);
            return ResponseEntity.ok(updatedShipment);
        } catch (RuntimeException e) {
            log.warn("Shipment not found for status update. ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Shipment not found with id: " + id);
        } catch (Exception e) {
            log.error("Failed to update shipment status. ID: {}, Status: {}. Error: {}", 
                    id, status, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to update shipment status");
        }
    }
    
    /**
     * Delete shipment
     * @param id shipment ID
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteShipment(@PathVariable String id) {
        log.info("Deleting shipment with ID: {}", id);
        try {
            shipmentService.deleteShipment(id);
            log.info("Successfully deleted shipment with ID: {}", id);
            return ResponseEntity.ok("Shipment deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete shipment with ID: {}. Error: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to delete shipment");
        }
    }
    
    /**
     * Global exception handler for file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<?> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        log.warn("File upload size exceeded: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body("File upload size exceeded. Maximum file size is 10MB and maximum request size is 50MB");
    }

    /**
     * Get images by shipment tracking number
     * @param trackingNumber shipment tracking number
     * @return list of images associated with the shipment
     */
    @GetMapping("/tracking/{trackingNumber}/images")
    public ResponseEntity<?> getImagesByTrackingNumber(@PathVariable String trackingNumber) {
        log.info("Retrieving images for shipment with tracking number: {}", trackingNumber);
        try {
            List<byte[]> images = shipmentService.getImagesByTrackingNumber(trackingNumber);
            
            if (images.isEmpty()) {
                log.info("No images found for shipment with tracking number: {}", trackingNumber);
                return ResponseEntity.ok().body("No images found for this shipment");
            }
            
            log.debug("Successfully retrieved {} images for shipment with tracking number: {}", 
                    images.size(), trackingNumber);
            return ResponseEntity.ok().body(images);
            
        } catch (RuntimeException e) {
            log.warn("Shipment not found with tracking number: {}", trackingNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Shipment not found with tracking number: " + trackingNumber);
        } catch (Exception e) {
            log.error("Failed to retrieve images for shipment with tracking number: {}. Error: {}", 
                    trackingNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve images for shipment");
        }
    }
}
