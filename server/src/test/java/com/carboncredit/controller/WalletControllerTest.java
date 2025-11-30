package com.carboncredit.controller;

import com.carboncredit.model.Wallet;
import com.carboncredit.repository.WalletRepository;
import com.carboncredit.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WalletControllerTest {

    @InjectMocks
    private WalletController walletController;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock Security Context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserDetailsImpl userDetails = new UserDetailsImpl("user1", "test@test.com", "password", null);
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    public void testDepositFunds() {
        Wallet.Transaction transaction = new Wallet.Transaction();
        Wallet wallet = new Wallet();
        wallet.setUserId("user1");
        wallet.setBalance(100.0);

        when(walletRepository.findByUserId("user1")).thenReturn(wallet);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        WalletController.DepositRequest req = new WalletController.DepositRequest();
        req.setAmount(50.0);

        ResponseEntity<Wallet> response = walletController.depositFunds(req);

        assertEquals(150.0, response.getBody().getBalance());
        assertEquals(1, response.getBody().getTransactions().size());
    }

    @Test
    public void testAddCarbonCredits() {
        Wallet wallet = new Wallet();
        wallet.setUserId("user1");
        wallet.setCarbonCreditBalance(10.0);

        when(walletRepository.findByUserId("user1")).thenReturn(wallet);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        WalletController.AddCreditsRequest req = new WalletController.AddCreditsRequest();
        req.setCredits(5.0);
        req.setAmount(100.0);

        ResponseEntity<Wallet> response = walletController.addCarbonCredits(req);

        assertEquals(15.0, response.getBody().getCarbonCreditBalance());
        assertEquals(1, response.getBody().getTransactions().size());
    }
}
