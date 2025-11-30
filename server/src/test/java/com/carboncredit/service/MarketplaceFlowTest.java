package com.carboncredit.service;

import com.carboncredit.model.*;
import com.carboncredit.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MarketplaceFlowTest {

    @InjectMocks
    private OrderMatchingService orderMatchingService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private com.carboncredit.service.WalletService walletService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCompleteTradingFlow() {
        // 1. Setup Users
        String buyerId = "buyer1";
        String sellerId = "seller1";

        // 4. Place SELL Order (100 @ $10)
        Order sellOrder = new Order();
        sellOrder.setId("sell1");
        sellOrder.setUserId(sellerId);
        sellOrder.setType(Order.OrderType.SELL);
        sellOrder.setQuantity(100.0);
        sellOrder.setPricePerUnit(10.0);
        sellOrder.setStatus(Order.OrderStatus.PENDING);
        sellOrder.setCreatedAt(LocalDateTime.now());

        // 5. Place BUY Order (50 @ $10) - Partial Fill Scenario
        Order buyOrder = new Order();
        buyOrder.setId("buy1");
        buyOrder.setUserId(buyerId);
        buyOrder.setType(Order.OrderType.BUY);
        buyOrder.setQuantity(50.0);
        buyOrder.setPricePerUnit(10.0);
        buyOrder.setStatus(Order.OrderStatus.PENDING);
        buyOrder.setCreatedAt(LocalDateTime.now().plusSeconds(1));

        // Mock Repository to return these orders
        when(orderRepository.findByTypeAndStatus(Order.OrderType.BUY, Order.OrderStatus.PENDING))
                .thenReturn(new ArrayList<>(Arrays.asList(buyOrder)));
        when(orderRepository.findByTypeAndStatus(Order.OrderType.SELL, Order.OrderStatus.PENDING))
                .thenReturn(new ArrayList<>(Arrays.asList(sellOrder)));

        // 6. Run Matching Engine
        orderMatchingService.matchOrders();

        // 7. Verify Results

        // Trade should be created for 50 units
        verify(tradeRepository, times(1)).save(argThat(trade -> trade.getBuyerId().equals(buyerId) &&
                trade.getSellerId().equals(sellerId) &&
                trade.getQuantity() == 50.0 &&
                trade.getPricePerUnit() == 10.0));

        // Buyer Wallet: +50 Credits (Money was deducted at order creation, no refund
        // needed as price matched)
        verify(walletService).updateBalance(buyerId, 0.0, 50.0);

        // Seller Wallet: +500 Money (Credits deducted at order creation)
        verify(walletService).updateBalance(sellerId, 500.0, 0.0);

        // Orders Status
        assertEquals(Order.OrderStatus.EXECUTED, buyOrder.getStatus()); // Fully filled
        assertEquals(Order.OrderStatus.PARTIAL, sellOrder.getStatus()); // Partially filled (50 remaining)
        assertEquals(50.0, sellOrder.getQuantity());
    }
}
