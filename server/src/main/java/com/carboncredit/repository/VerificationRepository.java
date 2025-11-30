package com.carboncredit.repository;

import com.carboncredit.model.Verification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface VerificationRepository extends MongoRepository<Verification, String> {
    List<Verification> findByVerifierId(String verifierId);
    List<Verification> findByProjectId(String projectId);
    List<Verification> findByStatus(Verification.VerificationStatus status);
}