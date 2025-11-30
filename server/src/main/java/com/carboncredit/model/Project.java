package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "projects")
public class Project {
    @org.springframework.data.annotation.Transient
    private User owner;

    @Id
    private String id;

    private String name;

    private String description;

    private String location;

    private ProjectType type;

    private ProjectStatus status;

    private String issuerId;

    private double totalCarbonCredits;

    private double availableCarbonCredits;

    private String documentationUrl;

    private boolean creditsGenerated = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ProjectType {
        // Reducing Projects (Generate Credits)
        RENEWABLE_ENERGY,
        SOLAR,
        WIND,
        REFORESTATION,
        GREEN_ENERGY,

        // Producing Projects (Consume Credits)
        MANUFACTURING,
        METAL,
        BURNING,
        INDUSTRIAL_BURNING,
        OTHER
    }

    public enum ProjectStatus {
        PENDING,
        VERIFIED,
        REJECTED,
        COMPLETED
    }
}