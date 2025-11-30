package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "usage_configs")
public class UsageConfig {
    @Id
    private String id;
    private String userId;
    private String broker;
    private String clientId;
    private String username;
    private String password;
    private String topic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
