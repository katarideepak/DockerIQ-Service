package com.dockeriq.service.service;

import com.dockeriq.service.model.AddShipment;
import com.dockeriq.service.model.Shipment;
import com.dockeriq.service.repository.ShipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Slf4j
@Service
public class ShipmentService {
    
    @Autowired
    private ShipmentRepository shipmentRepository;
    
    @Autowired
    private GridFSService gridFSService;
    
    @Autowired
    private TrackingNumberGeneratorService trackingNumberGenerator;
    
    /**
     * Create a new shipment with images from multipart form data
     * @param addShipment shipment data
     * @param images list of uploaded images
     * @return created shipment
     */
    public Shipment createShipmentWithImages(AddShipment addShipment, List<MultipartFile> images) {
        log.info("Creating shipment with images. Images count: {}", images != null ? images.size() : 0);
        try {
            // Process images if provided
            List<String> imageIds = null;
            if (images != null && !images.isEmpty()) {
                log.debug("Processing {} images for shipment", images.size());
                List<byte[]> imageData = images.stream()
                    .map(file -> {
                        try {
                            return file.getBytes();
                        } catch (IOException e) {
                            log.error("Failed to read image: {}. Error: {}", file.getOriginalFilename(), e.getMessage());
                            throw new RuntimeException("Failed to read image: " + file.getOriginalFilename(), e);
                        }
                    })
                    .toList();
                
                List<String> filenames = images.stream()
                    .map(MultipartFile::getOriginalFilename)
                    .toList();
                
                List<String> contentTypes = images.stream()
                    .map(MultipartFile::getContentType)
                    .toList();
                
                imageIds = gridFSService.storeMultipleImages(imageData, filenames, contentTypes);
                log.debug("Stored {} images in GridFS with IDs: {}", imageIds.size(), imageIds);
            }
            
            Shipment shipment = createShipmentEntity(addShipment, imageIds);
            log.info("Successfully created shipment with ID: {} and tracking number: {}", 
                    shipment.getId(), shipment.getTrackingNumber());
            return shipment;
            
        } catch (Exception e) {
            log.error("Failed to create shipment with images. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create shipment with images: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a new shipment without images
     * @param addShipment shipment data
     * @return created shipment
     */
    public Shipment createShipment(AddShipment addShipment) {
        log.info("Creating shipment without images for user: {}", addShipment.getCreatedBy());
        Shipment shipment = createShipmentEntity(addShipment, null);
        log.info("Successfully created shipment with ID: {} and tracking number: {}", 
                shipment.getId(), shipment.getTrackingNumber());
        return shipment;
    }
    
    /**
     * Create shipment entity with common logic
     * @param addShipment shipment data
     * @param imageIds list of GridFS image IDs
     * @return created shipment
     */
    private Shipment createShipmentEntity(AddShipment addShipment, List<String> imageIds) {
        log.debug("Creating shipment entity for user: {}", addShipment.getCreatedBy());
        
        // Create shipment entity
        Shipment shipment = new Shipment();
        shipment.setBasicInformation(addShipment.getBasicInformation());
        shipment.setCustomerFields(addShipment.getCustomerFields());
        shipment.setImageIds(imageIds);
        shipment.setNotes(addShipment.getNotes());
        shipment.setDeviceInformation(addShipment.getDeviceInformation());
        
        String trackingNumber = trackingNumberGenerator.generateTrackingNumber();
        shipment.setTrackingNumber(trackingNumber);
        log.debug("Generated tracking number: {}", trackingNumber);
        
        shipment.setStatus("CREATED");
        shipment.setCreatedAt(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        shipment.setCreatedBy(addShipment.getCreatedBy());
        shipment.setLastModifiedBy(addShipment.getCreatedBy());
        
        Shipment savedShipment = shipmentRepository.save(shipment);
        log.debug("Shipment saved to database with ID: {}", savedShipment.getId());
        return savedShipment;
    }
    
    /**
     * Get shipment by ID
     * @param id shipment ID
     * @return shipment if found
     */
    public Optional<Shipment> getShipmentById(String id) {
        log.debug("Retrieving shipment by ID: {}", id);
        Optional<Shipment> shipment = shipmentRepository.findById(id);
        if (shipment.isPresent()) {
            log.debug("Shipment found with ID: {}", id);
        } else {
            log.debug("Shipment not found with ID: {}", id);
        }
        return shipment;
    }
    
    
    /**
     * Get shipment by tracking number
     * @param trackingNumber tracking number
     * @return shipment if found
     */
    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
        log.debug("Retrieving shipment by tracking number: {}", trackingNumber);
        Optional<Shipment> shipment = shipmentRepository.findByTrackingNumber(trackingNumber);
        if (shipment.isPresent()) {
            log.debug("Shipment found with tracking number: {}", trackingNumber);
        } else {
            log.debug("Shipment not found with tracking number: {}", trackingNumber);
        }
        return shipment;
    }
    
    /**
     * Get all shipments
     * @return list of all shipments
     */
    public List<Shipment> getAllShipments() {
        log.debug("Retrieving all shipments");
        List<Shipment> shipments = shipmentRepository.findAll();
        log.debug("Retrieved {} shipments from database", shipments.size());
        return shipments;
    }
    
    /**
     * Update shipment status
     * @param id shipment ID
     * @param status new status
     * @param updatedBy user updating the shipment
     * @return updated shipment
     */
    public Shipment updateShipmentStatus(String id, String status, String updatedBy) {
        log.info("Updating shipment status. ID: {}, New status: {}, Updated by: {}", id, status, updatedBy);
        
        return shipmentRepository.findById(id)
            .map(shipment -> {
                log.debug("Found shipment with ID: {} for status update", id);
                shipment.setStatus(status);
                shipment.setUpdatedAt(LocalDateTime.now());
                shipment.setLastModifiedBy(updatedBy);
                Shipment updatedShipment = shipmentRepository.save(shipment);
                log.info("Successfully updated shipment status. ID: {}, New status: {}", id, status);
                return updatedShipment;
            })
            .orElseThrow(() -> {
                log.warn("Shipment not found for status update. ID: {}", id);
                return new RuntimeException("Shipment not found with id: " + id);
            });
    }
    
    /**
     * Delete shipment and associated images
     * @param id shipment ID
     */
    public void deleteShipment(String id) {
        log.info("Deleting shipment with ID: {}", id);
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(id);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            log.debug("Found shipment with ID: {} for deletion", id);
            
            // Delete associated images from GridFS
            if (shipment.getImageIds() != null && !shipment.getImageIds().isEmpty()) {
                log.debug("Deleting {} associated images from GridFS", shipment.getImageIds().size());
                for (String imageId : shipment.getImageIds()) {
                    try {
                        gridFSService.deleteImage(imageId);
                        log.debug("Deleted image from GridFS with ID: {}", imageId);
                    } catch (Exception e) {
                        log.warn("Failed to delete image from GridFS with ID: {}. Error: {}", imageId, e.getMessage());
                    }
                }
            }
            
            // Delete shipment
            shipmentRepository.deleteById(id);
            log.info("Successfully deleted shipment with ID: {}", id);
        } else {
            log.warn("Shipment not found for deletion. ID: {}", id);
        }
    }

    /**
     * Get images by shipment tracking number
     * @param trackingNumber shipment tracking number
     * @return list of images associated with the shipment
     */
    public List<byte[]> getImagesByTrackingNumber(String trackingNumber) {
        log.info("Retrieving images for shipment with tracking number: {}", trackingNumber);
        
        // First get the shipment to find associated image IDs
        Optional<Shipment> shipment = getShipmentByTrackingNumber(trackingNumber);
        if (shipment.isEmpty()) {
            log.warn("Shipment not found with tracking number: {}", trackingNumber);
            throw new RuntimeException("Shipment not found with tracking number: " + trackingNumber);
        }
        
        Shipment shipmentData = shipment.get();
        List<String> imageIds = shipmentData.getImageIds();
        
        if (imageIds == null || imageIds.isEmpty()) {
            log.info("No images found for shipment with tracking number: {}", trackingNumber);
            return new ArrayList<>();
        }
        
        log.debug("Found {} images for shipment with tracking number: {}", imageIds.size(), trackingNumber);
        
        // Retrieve all images and return them as a list
        List<byte[]> images = new ArrayList<>();
        for (String imageId : imageIds) {
            try {
                byte[] imageData = gridFSService.retrieveImage(imageId);
                images.add(imageData);
            } catch (Exception e) {
                log.warn("Failed to retrieve image with ID: {}. Skipping...", imageId);
            }
        }
        
        log.info("Successfully retrieved {} out of {} images for shipment with tracking number: {}", 
                images.size(), imageIds.size(), trackingNumber);
        return images;
    }
}
