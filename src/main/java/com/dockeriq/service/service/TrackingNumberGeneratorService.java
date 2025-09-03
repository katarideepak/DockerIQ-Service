package com.dockeriq.service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrackingNumberGeneratorService {
    
    @Autowired
    private MongoTemplate mongoTemplate;
    
    private static final String TRACKING_PREFIX = "DKIQ";
    private static final String DATE_FORMAT = "yyyyMMdd";
    private static final AtomicLong dailyCounter = new AtomicLong(1);
    
    /**
     * Generate a custom tracking number
     * Format: DKIQ + YYYYMMDD + 6-digit increment
     * Example: DKIQ20240115000001
     * 
     * @return generated tracking number
     */
    public String generateTrackingNumber() {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        
        // Get the next sequence number for today
        long sequenceNumber = getNextSequenceNumber(currentDate);
        
        // Format: DKIQ + YYYYMMDD + 6-digit sequence
        return String.format("%s%s%06d", TRACKING_PREFIX, currentDate, sequenceNumber);
    }
    
    /**
     * Get the next sequence number for a specific date
     * @param dateString date in YYYYMMDD format
     * @return next sequence number
     */
    private long getNextSequenceNumber(String dateString) {
        // Find the highest sequence number for today
        Query query = new Query(Criteria.where("trackingNumber")
            .regex("^" + TRACKING_PREFIX + dateString + ".*"));
        
        query.fields().include("trackingNumber");
        
        // Get all tracking numbers for today and find the highest sequence
        return mongoTemplate.find(query, String.class, "shipments")
            .stream()
            .mapToLong(trackingNumber -> {
                try {
                    // Extract sequence number from tracking number
                    String sequencePart = trackingNumber.substring(TRACKING_PREFIX.length() + DATE_FORMAT.length());
                    return Long.parseLong(sequencePart);
                } catch (Exception e) {
                    return 0L;
                }
            })
            .max()
            .orElse(0L) + 1;
    }
    
    /**
     * Generate tracking number with custom prefix
     * @param customPrefix custom prefix to use instead of DKIQ
     * @return generated tracking number
     */
    public String generateTrackingNumberWithPrefix(String customPrefix) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        long sequenceNumber = getNextSequenceNumberWithPrefix(customPrefix, currentDate);
        
        return String.format("%s%s%06d", customPrefix, currentDate, sequenceNumber);
    }
    
    /**
     * Get next sequence number for custom prefix
     * @param prefix custom prefix
     * @param dateString date in YYYYMMDD format
     * @return next sequence number
     */
    private long getNextSequenceNumberWithPrefix(String prefix, String dateString) {
        Query query = new Query(Criteria.where("trackingNumber")
            .regex("^" + prefix + dateString + ".*"));
        
        query.fields().include("trackingNumber");
        
        return mongoTemplate.find(query, String.class, "shipments")
            .stream()
            .mapToLong(trackingNumber -> {
                try {
                    String sequencePart = trackingNumber.substring(prefix.length() + DATE_FORMAT.length());
                    return Long.parseLong(sequencePart);
                } catch (Exception e) {
                    return 0L;
                }
            })
            .max()
            .orElse(0L) + 1;
    }
    
    /**
     * Validate if a tracking number format is valid
     * @param trackingNumber tracking number to validate
     * @return true if valid format
     */
    public boolean isValidTrackingNumberFormat(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.length() < 20) {
            return false;
        }
        
        // Check if it matches the expected format
        return trackingNumber.matches("^[A-Z]{4}\\d{8}\\d{6}$");
    }
    
    /**
     * Extract date from tracking number
     * @param trackingNumber tracking number
     * @return date string in YYYY-MM-DD format
     */
    public String extractDateFromTrackingNumber(String trackingNumber) {
        if (!isValidTrackingNumberFormat(trackingNumber)) {
            throw new IllegalArgumentException("Invalid tracking number format");
        }
        
        String datePart = trackingNumber.substring(4, 12); // Extract YYYYMMDD part
        return String.format("%s-%s-%s", 
            datePart.substring(0, 4), 
            datePart.substring(4, 6), 
            datePart.substring(6, 8));
    }
}
