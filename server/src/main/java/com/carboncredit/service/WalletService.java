package com.carboncredit.service;

import com.carboncredit.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Atomically updates the wallet balance and carbon credit balance.
     * Throws an exception if the wallet is not found or if there are insufficient
     * funds/credits (optional check).
     *
     * @param userId      The user ID.
     * @param moneyDelta  The amount to add (positive) or subtract (negative) from
     *                    the money balance.
     * @param creditDelta The amount to add (positive) or subtract (negative) from
     *                    the carbon credit balance.
     * @return The updated Wallet object.
     */
    @Transactional
    public Wallet updateBalance(String userId, double moneyDelta, double creditDelta) {
        System.out.println("Updating wallet for user: " + userId + ", moneyDelta: " + moneyDelta + ", creditDelta: "
                + creditDelta);

        Query query = new Query(Criteria.where("userId").is(userId));
        Update update = new Update();

        if (moneyDelta != 0) {
            update.inc("balance", moneyDelta);
        }
        if (creditDelta != 0) {
            update.inc("carbonCreditBalance", creditDelta);
        }

        if (moneyDelta < 0) {
            // Ensure sufficient monetary balance (cannot go negative)
            query.addCriteria(Criteria.where("balance").gte(Math.abs(moneyDelta)));
        }

        // Credit balance can go negative (like a carbon tax/debt)
        // No check needed for creditDelta < 0

        Wallet updatedWallet = mongoTemplate.findAndModify(
                query,
                update,
                org.springframework.data.mongodb.core.FindAndModifyOptions.options().returnNew(true),
                Wallet.class);

        if (updatedWallet == null) {
            System.err.println(
                    "Wallet update failed: Insufficient funds/credits or wallet not found for user: " + userId);
            throw new RuntimeException("Wallet update failed: Insufficient funds/credits or wallet not found.");
        }

        System.out.println("Wallet updated successfully. New Balance: " + updatedWallet.getBalance()
                + ", New Credit Balance: " + updatedWallet.getCarbonCreditBalance());

        return updatedWallet;
    }

    // --- Locked Balance Logic ---

    @Transactional
    public void reserveFunds(String userId, double amount) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("balance").gte(amount));

        Update update = new Update();
        update.inc("balance", -amount);
        update.inc("moneyLocked", amount);

        Wallet updated = mongoTemplate.findAndModify(query, update, Wallet.class);
        if (updated == null) {
            throw new RuntimeException("Insufficient funds to reserve for user: " + userId);
        }
    }

    @Transactional
    public void reserveCredits(String userId, double amount) {
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("carbonCreditBalance").gte(amount));

        Update update = new Update();
        update.inc("carbonCreditBalance", -amount);
        update.inc("creditLocked", amount);

        Wallet updated = mongoTemplate.findAndModify(query, update, Wallet.class);
        if (updated == null) {
            throw new RuntimeException("Insufficient credits to reserve for user: " + userId);
        }
    }

    @Transactional
    public void releaseFunds(String userId, double amount) {
        Query query = new Query(Criteria.where("userId").is(userId));
        // Ideally check if moneyLocked >= amount, but for now assume correct logic

        Update update = new Update();
        update.inc("moneyLocked", -amount);
        update.inc("balance", amount);

        mongoTemplate.updateFirst(query, update, Wallet.class);
    }

    @Transactional
    public void releaseCredits(String userId, double amount) {
        Query query = new Query(Criteria.where("userId").is(userId));

        Update update = new Update();
        update.inc("creditLocked", -amount);
        update.inc("carbonCreditBalance", amount);

        mongoTemplate.updateFirst(query, update, Wallet.class);
    }

    @Transactional
    public void processTrade(String buyerId, String sellerId, double quantity, double price) {
        double totalCost = quantity * price;

        // 1. Buyer: Decrease Money Locked, Increase Credit Balance
        Update buyerUpdate = new Update();
        buyerUpdate.inc("moneyLocked", -totalCost);
        buyerUpdate.inc("carbonCreditBalance", quantity);

        // Add Transaction Record for Buyer
        Wallet.Transaction buyerTx = new Wallet.Transaction();
        buyerTx.setId(java.util.UUID.randomUUID().toString());
        buyerTx.setType(Wallet.Transaction.TransactionType.PURCHASE);
        buyerTx.setAmount(-totalCost); // Negative for spending
        buyerTx.setCarbonCredits(quantity);
        buyerTx.setDescription("Bought " + quantity + " credits @ $" + price);
        buyerTx.setTimestamp(System.currentTimeMillis());
        buyerUpdate.push("transactions", buyerTx);

        mongoTemplate.updateFirst(new Query(Criteria.where("userId").is(buyerId)), buyerUpdate, Wallet.class);

        // 2. Seller: Decrease Credit Locked, Increase Money Balance
        Update sellerUpdate = new Update();
        sellerUpdate.inc("creditLocked", -quantity);
        sellerUpdate.inc("balance", totalCost);

        // Add Transaction Record for Seller
        Wallet.Transaction sellerTx = new Wallet.Transaction();
        sellerTx.setId(java.util.UUID.randomUUID().toString());
        sellerTx.setType(Wallet.Transaction.TransactionType.SALE);
        sellerTx.setAmount(totalCost); // Positive for earning
        sellerTx.setCarbonCredits(-quantity); // Negative for selling credits
        sellerTx.setDescription("Sold " + quantity + " credits @ $" + price);
        sellerTx.setTimestamp(System.currentTimeMillis());
        sellerUpdate.push("transactions", sellerTx);

        mongoTemplate.updateFirst(new Query(Criteria.where("userId").is(sellerId)), sellerUpdate, Wallet.class);
    }

    // Helper to create wallet if not exists (idempotent)
    public void createWalletIfNotExists(String userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        if (!mongoTemplate.exists(query, Wallet.class)) {
            Wallet wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(0.0);
            wallet.setCarbonCreditBalance(0.0);
            wallet.setMoneyLocked(0.0);
            wallet.setCreditLocked(0.0);
            mongoTemplate.save(wallet);
        }
    }

    @Transactional
    public void retireCredits(String userId, double quantity) {
        // 1. Deduct credits from balance
        Query query = new Query(Criteria.where("userId").is(userId));
        query.addCriteria(Criteria.where("carbonCreditBalance").gte(quantity));

        Update update = new Update();
        update.inc("carbonCreditBalance", -quantity);

        // 2. Add Transaction Record
        Wallet.Transaction tx = new Wallet.Transaction();
        tx.setId(java.util.UUID.randomUUID().toString());
        tx.setType(Wallet.Transaction.TransactionType.CREDIT_RETIREMENT);
        tx.setAmount(0.0); // No money involved
        tx.setCarbonCredits(-quantity);
        tx.setDescription("Retired " + quantity + " credits");
        tx.setTimestamp(System.currentTimeMillis());
        update.push("transactions", tx);

        Wallet updated = mongoTemplate.findAndModify(query, update, Wallet.class);
        if (updated == null) {
            throw new RuntimeException("Insufficient credits to retire for user: " + userId);
        }
    }
}
