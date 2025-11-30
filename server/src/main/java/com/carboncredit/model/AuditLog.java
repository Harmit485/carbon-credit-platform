package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "audit_logs")
public class AuditLog {
    @Id
    private String id;
    
    private String userId;
    
    private ActionType action;
    
    private String entityType;
    
    private String entityId;
    
    private String details;
    
    private String ipAddress;
    
    @CreatedDate
    private LocalDateTime timestamp;
    
    public enum ActionType {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        TRADE,
        VERIFY,
        RETIRE
    }
}