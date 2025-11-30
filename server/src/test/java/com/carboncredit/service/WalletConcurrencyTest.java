package com.carboncredit.service;

import com.carboncredit.model.Wallet;
import com.carboncredit.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class WalletConcurrencyTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    public void setup() {
        walletRepository.deleteAll();
    }

    @Test
    public void testConcurrentBalanceUpdates() throws InterruptedException {
        String userId = "concurrentUser";

        // Initial setup
        walletService.createWalletIfNotExists(userId);
        walletService.updateBalance(userId, 1000.0, 1000.0);

        int numberOfThreads = 20;
        int operationsPerThread = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        try {
                            // Each op: -1 money, -1 credit
                            walletService.updateBalance(userId, -1.0, -1.0);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // Expected:
        // Initial: 1000
        // Total Ops: 20 * 50 = 1000
        // Final: 0

        Wallet wallet = walletRepository.findByUserId(userId);
        System.out.println("Success: " + successCount.get());
        System.out.println("Fail: " + failCount.get());
        System.out.println("Final Balance: " + wallet.getBalance());
        System.out.println("Final Credits: " + wallet.getCarbonCreditBalance());

        assertEquals(0.0, wallet.getBalance(), 0.001);
        assertEquals(0.0, wallet.getCarbonCreditBalance(), 0.001);
        assertEquals(1000, successCount.get());
    }

    @Test
    public void testConcurrentOverdraftProtection() throws InterruptedException {
        String userId = "overdraftUser";

        // Initial setup: Only 100 funds
        walletService.createWalletIfNotExists(userId);
        walletService.updateBalance(userId, 100.0, 0.0);

        int numberOfThreads = 10;
        // Try to withdraw 20 each. Total demand = 200. Only 5 should succeed.
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    walletService.updateBalance(userId, -20.0, 0.0);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Wallet wallet = walletRepository.findByUserId(userId);
        System.out.println("Overdraft Test - Success: " + successCount.get());
        System.out.println("Overdraft Test - Fail: " + failCount.get());
        System.out.println("Overdraft Test - Final Balance: " + wallet.getBalance());

        assertEquals(0.0, wallet.getBalance(), 0.001);
        assertEquals(5, successCount.get()); // 100 / 20 = 5
        assertEquals(5, failCount.get());
    }
}
