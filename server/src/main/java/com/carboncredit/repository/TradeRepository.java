package com.carboncredit.repository;

import com.carboncredit.model.Trade;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TradeRepository extends MongoRepository<Trade, String> {
    List<Trade> findByBuyerId(String buyerId);

    List<Trade> findByBuyOrderId(String buyOrderId);

    List<Trade> findBySellerId(String sellerId);

    List<Trade> findBySellOrderId(String sellOrderId);

    List<Trade> findTop10ByOrderByExecutedAtDesc();

    List<Trade> findByBuyerIdOrSellerId(String buyerId, String sellerId);

    long countByExecutedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    java.util.Optional<Trade> findTopByOrderByExecutedAtDesc();
}