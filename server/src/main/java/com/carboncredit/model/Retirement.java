package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "retirements")
public class Retirement {
    @Id
    private String id;

    private String userId;

    // private String creditId; // Removed as credits are fungible

    private double quantity;

    private String beneficiaryName;

    private String retirementReason;

    private String certificateUrl;

    @CreatedDate
    private LocalDateTime retiredAt;
}