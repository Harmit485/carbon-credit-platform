package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "carbon_credits")
public class CarbonCredit {
    @Id
    private String id;
    
    private String serialNumber;
    
    private String projectId;
    
    private String ownerId;
    
    private double quantity;
    
    private int vintageYear;
    
    private double pricePerUnit;
    
    private CreditStatus status;
    
    private String verificationId;
    
    @CreatedDate
    private LocalDateTime issuedAt;
    
    private LocalDateTime verifiedAt;
    
    private LocalDateTime retiredAt;
    
    public enum CreditStatus {
        ISSUED,
        VERIFIED,
        LISTED,
        TRANSFERRED,
        RETIRED
    }
}