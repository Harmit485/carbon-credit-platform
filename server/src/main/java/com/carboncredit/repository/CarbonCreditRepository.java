package com.carboncredit.repository;

import com.carboncredit.model.CarbonCredit;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CarbonCreditRepository extends MongoRepository<CarbonCredit, String> {
    List<CarbonCredit> findByOwnerId(String ownerId);

    List<CarbonCredit> findByProjectId(String projectId);

    List<CarbonCredit> findByStatus(CarbonCredit.CreditStatus status);

    long countByStatus(CarbonCredit.CreditStatus status);

    long countByIssuedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}