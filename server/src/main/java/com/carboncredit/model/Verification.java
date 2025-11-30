package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "verifications")
public class Verification {
    @Id
    private String id;
    
    private String projectId;
    
    private String verifierId;
    
    private VerificationStatus status;
    
    private String comments;
    
    private String documentationUrl;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    public enum VerificationStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}