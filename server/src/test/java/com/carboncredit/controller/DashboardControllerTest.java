package com.carboncredit.controller;

import com.carboncredit.model.*;
import com.carboncredit.repository.*;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class DashboardControllerTest {

    @InjectMocks
    private DashboardController dashboardController;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CarbonCreditRepository carbonCreditRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private RetirementRepository retirementRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetDashboardStats() {
        when(projectRepository.count()).thenReturn(10L);
        when(carbonCreditRepository.count()).thenReturn(100L);
        when(userRepository.count()).thenReturn(50L);
        when(tradeRepository.count()).thenReturn(20L);
        when(retirementRepository.count()).thenReturn(5L);
        when(carbonCreditRepository.countByStatus(any(CarbonCredit.CreditStatus.class))).thenReturn(10L);
        when(tradeRepository.findTop10ByOrderByExecutedAtDesc()).thenReturn(new ArrayList<>());
        when(projectRepository.findAll()).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = dashboardController.getDashboardStats();

        assertEquals(200, response.getStatusCode().value());
        DashboardController.DashboardStats stats = (DashboardController.DashboardStats) response.getBody();
        assertNotNull(stats);
        assertEquals(10L, stats.getTotalProjects());
        assertEquals(100L, stats.getTotalCredits());
    }

    @Test
    public void testGetUserDashboardStats() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserDetailsImpl userDetails = new UserDetailsImpl("user1", "user@test.com", "password", new HashSet<>());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        Wallet wallet = new Wallet();
        wallet.setCarbonCreditBalance(50.0);
        when(walletRepository.findByUserId("user1")).thenReturn(wallet);
        when(projectRepository.findByIssuerId("user1")).thenReturn(new ArrayList<>());
        when(carbonCreditRepository.findByOwnerId("user1")).thenReturn(new ArrayList<>());
        when(tradeRepository.findByBuyerId("user1")).thenReturn(new ArrayList<>());
        when(tradeRepository.findBySellerId("user1")).thenReturn(new ArrayList<>());
        when(retirementRepository.findByUserId("user1")).thenReturn(new ArrayList<>());
        when(orderRepository.findByUserId("user1")).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = dashboardController.getUserDashboardStats();

        assertEquals(200, response.getStatusCode().value());
        DashboardController.UserDashboardStats stats = (DashboardController.UserDashboardStats) response.getBody();
        assertNotNull(stats);
        assertEquals(50.0, stats.getCreditBalance());
    }

}
