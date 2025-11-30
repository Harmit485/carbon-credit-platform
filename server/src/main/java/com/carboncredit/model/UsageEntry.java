package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "usage_entries")
public class UsageEntry {
    @Id
    private String id;
    private String userId;
    private Double co2KgDelta;
    private LocalDateTime timestamp;
    private String rawPayload; // Optional: store raw payload for debugging
}
