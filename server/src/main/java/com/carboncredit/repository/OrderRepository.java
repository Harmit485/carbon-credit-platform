package com.carboncredit.repository;

import com.carboncredit.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByType(Order.OrderType type);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByTypeAndStatus(Order.OrderType type, Order.OrderStatus status);
}