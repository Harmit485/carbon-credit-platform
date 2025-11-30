package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "wallets")
public class Wallet {
    @Id
    private String id;

    private String userId;

    private double balance; // Monetary balance

    private double carbonCreditBalance; // Carbon credit balance

    private double moneyLocked; // Locked funds for active buy orders
    private double creditLocked; // Locked credits for active sell orders

    private List<Transaction> transactions = new ArrayList<>();

    @Data
    public static class Transaction {
        private String id;
        private TransactionType type;
        private double amount;
        private double carbonCredits; // For credit transactions
        private String relatedEntityId;
        private String description;
        private long timestamp;

        public enum TransactionType {
            DEPOSIT,
            WITHDRAWAL,
            PURCHASE,
            SALE,
            CREDIT_PURCHASE,
            CREDIT_SALE,
            CREDIT_RETIREMENT
        }
    }
}