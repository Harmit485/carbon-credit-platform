package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "trades")
public class Trade {
    @Id
    private String id;
    
    private String buyOrderId;
    
    private String sellOrderId;
    
    private String buyerId;
    
    private String sellerId;
    
    private String creditId;
    
    private double quantity;
    
    private double pricePerUnit;
    
    private double totalAmount;
    
    @CreatedDate
    private LocalDateTime executedAt;
}