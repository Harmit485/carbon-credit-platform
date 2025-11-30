package com.carboncredit.controller;

import com.carboncredit.model.Order;
import com.carboncredit.repository.OrderRepository;
import com.carboncredit.repository.TradeRepository;
import com.carboncredit.service.OrderMatchingService;
import com.carboncredit.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MarketplaceControllerTest {

    @InjectMocks
    private MarketplaceController marketplaceController;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private OrderMatchingService orderMatchingService;

    @Mock
    private PricingService pricingService;

    @Mock
    private com.carboncredit.service.WalletService walletService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testTriggerMatching() {
        ResponseEntity<?> response = marketplaceController.triggerMatching();
        assertEquals(200, response.getStatusCode().value());
        verify(orderMatchingService, times(1)).matchOrders();
    }

    @Test
    public void testCreateOrder_Buy() {
        Order order = new Order();
        order.setType(Order.OrderType.BUY);
        order.setQuantity(10);
        order.setPricePerUnit(10);

        // Mock Security Context
        org.springframework.security.core.Authentication authentication = mock(
                org.springframework.security.core.Authentication.class);
        org.springframework.security.core.context.SecurityContext securityContext = mock(
                org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        com.carboncredit.security.UserDetailsImpl userDetails = new com.carboncredit.security.UserDetailsImpl("user1",
                "user1@test.com", "password", new java.util.HashSet<>());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        marketplaceController.createOrder(order);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testCreateOrder_Sell() {
        Order order = new Order();
        order.setType(Order.OrderType.SELL);
        order.setQuantity(10);
        order.setPricePerUnit(10);

        // Mock Security Context
        org.springframework.security.core.Authentication authentication = mock(
                org.springframework.security.core.Authentication.class);
        org.springframework.security.core.context.SecurityContext securityContext = mock(
                org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

        com.carboncredit.security.UserDetailsImpl userDetails = new com.carboncredit.security.UserDetailsImpl("user1",
                "user1@test.com", "password", new java.util.HashSet<>());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        marketplaceController.createOrder(order);

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    public void testGetDynamicPrice() {
        when(pricingService.calculateDynamicPrice(10.0)).thenReturn(11.0);

        ResponseEntity<Double> response = marketplaceController.getDynamicPrice(10.0);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(11.0, response.getBody());
    }
}
