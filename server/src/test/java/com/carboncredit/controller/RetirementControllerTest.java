package com.carboncredit.controller;

import com.carboncredit.model.CarbonCredit;
import com.carboncredit.model.Wallet;
import com.carboncredit.repository.CarbonCreditRepository;
import com.carboncredit.repository.RetirementRepository;
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

public class RetirementControllerTest {

    @InjectMocks
    private RetirementController retirementController;

    @Mock
    private RetirementRepository retirementRepository;

    @Mock
    private CarbonCreditRepository carbonCreditRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private com.carboncredit.service.WalletService walletService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserDetailsImpl userDetails = new UserDetailsImpl("user1", "test@test.com", "password", null);
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    public void testRetireCreditsInsufficientWalletCredits() {
        // Mock walletService to throw exception when insufficient credits
        doThrow(new RuntimeException("Insufficient credits")).when(walletService).retireCredits(anyString(),
                anyDouble());

        RetirementController.RetirementRequest request = new RetirementController.RetirementRequest();
        // request.setCreditId("credit1"); // Removed
        request.setQuantity(10.0);

        ResponseEntity<?> response = retirementController.retireCredits(request);
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().toString().contains("Insufficient credits in wallet"));
    }

    @Test
    public void testRetireCreditsSuccessUpdatesWalletAndCredit() {
        // Mock walletService to succeed
        doNothing().when(walletService).retireCredits(anyString(), anyDouble());

        when(retirementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RetirementController.RetirementRequest request = new RetirementController.RetirementRequest();
        // request.setCreditId("credit1"); // Removed
        request.setQuantity(10.0);

        ResponseEntity<?> response = retirementController.retireCredits(request);
        assertEquals(200, response.getStatusCode().value());

        // Ensure walletService was called to retire credits
        verify(walletService, times(1)).retireCredits("user1", 10.0);
    }
}