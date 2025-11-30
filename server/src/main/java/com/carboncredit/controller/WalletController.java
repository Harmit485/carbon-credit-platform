package com.carboncredit.controller;

import com.carboncredit.model.Wallet;
import com.carboncredit.repository.WalletRepository;
import com.carboncredit.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping({ "/api/wallet", "/wallet" })
public class WalletController {

    @Autowired
    private WalletRepository walletRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Wallet> getWallet() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String userId = userDetails.getId();
        Wallet wallet = walletRepository.findByUserId(userId);

        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getTransactions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String userId = userDetails.getId();
        Wallet wallet = walletRepository.findByUserId(userId);

        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(wallet.getTransactions());
    }

    @PostMapping("/deposit")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Wallet> depositFunds(@RequestBody DepositRequest depositRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String userId = userDetails.getId();
        Wallet wallet = walletRepository.findByUserId(userId);

        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(0.0);
            wallet.setCarbonCreditBalance(0.0);
        }

        // Add deposit amount to balance
        double newBalance = wallet.getBalance() + depositRequest.getAmount();
        wallet.setBalance(newBalance);

        // Add transaction record
        Wallet.Transaction transaction = new Wallet.Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setType(Wallet.Transaction.TransactionType.DEPOSIT);
        transaction.setAmount(depositRequest.getAmount());
        transaction.setDescription("Funds added to wallet");
        transaction.setTimestamp(System.currentTimeMillis());
        wallet.getTransactions().add(transaction);

        // Save updated wallet
        Wallet updatedWallet = walletRepository.save(wallet);

        return ResponseEntity.ok(updatedWallet);
    }

    // Add carbon credits to wallet
    @PostMapping("/add-credits")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Wallet> addCarbonCredits(@RequestBody AddCreditsRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String userId = userDetails.getId();
        Wallet wallet = walletRepository.findByUserId(userId);

        if (wallet == null) {
            wallet = new Wallet();
            wallet.setUserId(userId);
            wallet.setBalance(0.0);
            wallet.setCarbonCreditBalance(0.0);
        }

        // Add credits to balance
        double newCreditBalance = wallet.getCarbonCreditBalance() + request.getCredits();
        wallet.setCarbonCreditBalance(newCreditBalance);

        // Add transaction record
        Wallet.Transaction transaction = new Wallet.Transaction();
        transaction.setId(UUID.randomUUID().toString());
        transaction.setType(Wallet.Transaction.TransactionType.CREDIT_PURCHASE);
        transaction.setCarbonCredits(request.getCredits());
        transaction.setAmount(request.getAmount()); // Monetary amount spent
        transaction.setDescription("Carbon credits added to wallet: " + request.getCredits() + " credits");
        transaction.setTimestamp(System.currentTimeMillis());
        wallet.getTransactions().add(transaction);

        // Save updated wallet
        Wallet updatedWallet = walletRepository.save(wallet);

        return ResponseEntity.ok(updatedWallet);
    }

    // Request DTO for deposit
    public static class DepositRequest {
        private double amount;

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }

    // Request DTO for adding credits
    public static class AddCreditsRequest {
        private double credits;
        private double amount; // Monetary amount spent

        public double getCredits() {
            return credits;
        }

        public void setCredits(double credits) {
            this.credits = credits;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }
    }
}