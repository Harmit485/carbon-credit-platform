package com.carboncredit.repository;

import com.carboncredit.model.Retirement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RetirementRepository extends MongoRepository<Retirement, String> {
    List<Retirement> findByUserId(String userId);
    // List<Retirement> findByCreditId(String creditId); // Removed as creditId was
    // removed from Retirement model
}