package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId;

    // private String creditId; // Removed: Credits are fungible now

    private OrderType type;

    private OrderStatus status;

    private double quantity;

    private double pricePerUnit;

    private double totalAmount;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public enum OrderType {
        BUY,
        SELL
    }

    public enum OrderStatus {
        PENDING,
        PARTIAL,
        EXECUTED,
        CANCELLED
    }
}