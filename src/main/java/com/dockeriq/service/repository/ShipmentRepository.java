package com.dockeriq.service.repository;

import com.dockeriq.service.model.Shipment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends MongoRepository<Shipment, String> {
    
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    boolean existsByTrackingNumber(String trackingNumber);
}
