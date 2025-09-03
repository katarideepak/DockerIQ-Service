package com.dockeriq.service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.dockeriq.service.model.UserDetails;

@Repository
public interface UserDetailsRepository extends MongoRepository<UserDetails, String> {
    Optional<UserDetails> findByEmail(String email);
} 