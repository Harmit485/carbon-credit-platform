package com.carboncredit.repository;

import com.carboncredit.model.UsageEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UsageEntryRepository extends MongoRepository<UsageEntry, String> {
    List<UsageEntry> findByUserIdOrderByTimestampDesc(String userId);

    List<UsageEntry> findTop5ByUserIdOrderByTimestampDesc(String userId);
}
