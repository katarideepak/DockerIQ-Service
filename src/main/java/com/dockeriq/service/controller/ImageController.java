package com.dockeriq.service.controller;

import com.dockeriq.service.service.GridFSService;
import com.dockeriq.service.service.ShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/images")
@CrossOrigin(origins = "*")
public class ImageController {
    
    @Autowired
    private GridFSService gridFSService;
    
    @Autowired
    private ShipmentService shipmentService;
    
    /**
     * Get individual image by image ID with optimized response
     * @param imageId GridFS image ID
     * @return image with proper content type and caching headers
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<?> getImageById(@PathVariable String imageId) {
        log.info("API: Retrieving image with ID: {}", imageId);
        try {
            // Get image metadata first
            Map<String, Object> metadata = shipmentService.getImageMetadataById(imageId);
            if (metadata == null) {
                log.warn("API: Image not found with ID: {}", imageId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Image not found with ID: " + imageId);
            }
            
            // Retrieve image data directly from GridFS
            byte[] imageBytes = gridFSService.retrieveImage(imageId);
            String contentType = (String) metadata.get("contentType");
            
            log.debug("API: Successfully retrieved image with ID: {}, Content-Type: {}, Size: {} bytes", 
                    imageId, contentType, imageBytes.length);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Cache-Control", "public, max-age=7200") // Cache for 2 hours
                .header("ETag", "\"" + imageId + "\"")
                .header("Last-Modified", metadata.get("uploadDate").toString())
                .body(imageBytes);
            
        } catch (Exception e) {
            log.error("API: Failed to retrieve image with ID: {}. Error: {}", imageId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve image");
        }
    }
    
    /**
     * Get image metadata by image ID (fast endpoint for UI)
     * @param imageId GridFS image ID
     * @return image metadata without actual image data
     */
    @GetMapping("/{imageId}/info")
    public ResponseEntity<?> getImageInfo(@PathVariable String imageId) {
        log.info("API: Retrieving image info with ID: {}", imageId);
        try {
            Map<String, Object> metadata = shipmentService.getImageMetadataById(imageId);
            if (metadata == null) {
                log.warn("API: Image not found with ID: {}", imageId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Image not found with ID: " + imageId);
            }
            
            // Add API URL for frontend
            metadata.put("apiUrl", "/api/images/" + imageId);
            
            log.debug("API: Successfully retrieved image info with ID: {}", imageId);
            return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=3600") // Cache metadata for 1 hour
                .body(metadata);
            
        } catch (Exception e) {
            log.error("API: Failed to retrieve image info with ID: {}. Error: {}", imageId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve image info");
        }
    }
    
    /**
     * Get all images by shipment tracking number (optimized for UI)
     * @param trackingNumber shipment tracking number
     * @return list of image metadata with API URLs
     */
    @GetMapping("tracking/{trackingNumber}")
    public ResponseEntity<?> getImagesByTrackingNumber(@PathVariable String trackingNumber) {
        log.info("API: Retrieving images for shipment with tracking number: {}", trackingNumber);
        try {
            List<Map<String, Object>> imageMetadata = shipmentService.getImageMetadataByTrackingNumber(trackingNumber);
            
            if (imageMetadata.isEmpty()) {
                log.info("API: No images found for shipment with tracking number: {}", trackingNumber);
                return ResponseEntity.ok().body("No images found for this shipment");
            }
            
            log.debug("API: Found {} images for shipment with tracking number: {}", imageMetadata.size(), trackingNumber);
            
            // Add API URLs for each image
            List<Map<String, Object>> apiImages = new ArrayList<>();
            for (Map<String, Object> metadata : imageMetadata) {
                Map<String, Object> apiImage = new HashMap<>(metadata);
                String imageId = (String) metadata.get("id");
                apiImage.put("apiUrl", "/images/" + imageId);
                apiImage.put("infoUrl", "/images/" + imageId + "/info");
                apiImages.add(apiImage);
            }
            
            log.info("API: Successfully prepared {} images for shipment with tracking number: {}", 
                    apiImages.size(), trackingNumber);
            return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=1800") // Cache for 30 minutes
                .body(apiImages);
            
        } catch (RuntimeException e) {
            log.warn("API: Shipment not found with tracking number: {}", trackingNumber);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Shipment not found with tracking number: " + trackingNumber);
        } catch (Exception e) {
            log.error("API: Failed to retrieve images for shipment with tracking number: {}. Error: {}", 
                    trackingNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve images for shipment");
        }
    }
    
    /**
     * Stream image by image ID (for large images)
     * @param imageId GridFS image ID
     * @return streaming image response
     */
    @GetMapping("/{imageId}/stream")
    public ResponseEntity<?> streamImageById(@PathVariable String imageId) {
        log.info("API: Streaming image with ID: {}", imageId);
        try {
            // Get image metadata first
            Map<String, Object> metadata = shipmentService.getImageMetadataById(imageId);
            if (metadata == null) {
                log.warn("API: Image not found with ID: {}", imageId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Image not found with ID: " + imageId);
            }
            
            // Use GridFS streaming for better performance
            var inputStream = gridFSService.streamImage(imageId);
            String contentType = (String) metadata.get("contentType");
            Long size = (Long) metadata.get("size");
            
            log.debug("API: Successfully streaming image with ID: {}, Content-Type: {}, Size: {} bytes", 
                    imageId, contentType, size);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header("Cache-Control", "public, max-age=7200") // Cache for 2 hours
                .header("ETag", "\"" + imageId + "\"")
                .header("Last-Modified", metadata.get("uploadDate").toString())
                .contentLength(size)
                .body(inputStream);
            
        } catch (Exception e) {
            log.error("API: Failed to stream image with ID: {}. Error: {}", imageId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to stream image");
        }
    }
}
