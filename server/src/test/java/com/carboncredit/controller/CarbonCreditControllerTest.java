package com.carboncredit.controller;

import com.carboncredit.model.CarbonCredit;
import com.carboncredit.repository.CarbonCreditRepository;
import com.carboncredit.service.CreditService;
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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CarbonCreditControllerTest {

    @InjectMocks
    private CarbonCreditController carbonCreditController;

    @Mock
    private CarbonCreditRepository carbonCreditRepository;

    @Mock
    private CreditService creditService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateCredit() {
        CarbonCredit credit = new CarbonCredit();
        credit.setProjectId("proj1");
        credit.setQuantity(100.0);

        when(carbonCreditRepository.save(any(CarbonCredit.class))).thenReturn(credit);

        CarbonCredit createdCredit = carbonCreditController.createCredit(credit);

        assertEquals("proj1", createdCredit.getProjectId());
        verify(carbonCreditRepository, times(1)).save(credit);
    }

    @Test
    public void testVerifyCredit() {
        String creditId = "credit1";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "VERIFIED");

        CarbonCredit credit = new CarbonCredit();
        credit.setId(creditId);
        credit.setStatus(CarbonCredit.CreditStatus.ISSUED);

        when(carbonCreditRepository.findById(creditId)).thenReturn(Optional.of(credit));
        when(carbonCreditRepository.save(any(CarbonCredit.class))).thenReturn(credit);

        ResponseEntity<?> response = carbonCreditController.verifyCredit(creditId, requestBody);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(CarbonCredit.CreditStatus.VERIFIED, credit.getStatus());
        verify(carbonCreditRepository, times(1)).save(credit);
    }

    @Test
    public void testGenerateCredits() {
        Map<String, Object> request = new HashMap<>();
        request.put("projectId", "proj1");
        request.put("amount", 100.0);

        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserDetailsImpl userDetails = new UserDetailsImpl("user1", "user@test.com", "password", new HashSet<>());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        doNothing().when(creditService).generateCredits("proj1", 100.0, "user1");

        ResponseEntity<?> response = carbonCreditController.generateCredits(request);

        assertEquals(200, response.getStatusCode().value());
        verify(creditService, times(1)).generateCredits("proj1", 100.0, "user1");
    }

    @Test
    public void testConsumeCredits() {
        Map<String, Object> request = new HashMap<>();
        request.put("projectId", "proj1");
        request.put("amount", 50.0);

        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserDetailsImpl userDetails = new UserDetailsImpl("user1", "user@test.com", "password", new HashSet<>());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        doNothing().when(creditService).consumeCredits("proj1", 50.0, "user1");

        ResponseEntity<?> response = carbonCreditController.consumeCredits(request);

        assertEquals(200, response.getStatusCode().value());
        verify(creditService, times(1)).consumeCredits("proj1", 50.0, "user1");
    }
}
