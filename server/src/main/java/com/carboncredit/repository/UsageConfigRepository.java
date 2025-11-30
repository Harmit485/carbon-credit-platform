package com.carboncredit.repository;

import com.carboncredit.model.UsageConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface UsageConfigRepository extends MongoRepository<UsageConfig, String> {
    Optional<UsageConfig> findByUserId(String userId);
}
