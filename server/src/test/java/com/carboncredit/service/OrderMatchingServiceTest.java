package com.carboncredit.service;

import com.carboncredit.model.Order;
import com.carboncredit.model.Trade;
import com.carboncredit.repository.OrderRepository;
import com.carboncredit.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class OrderMatchingServiceTest {

    @InjectMocks
    private OrderMatchingService orderMatchingService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private OrderBook orderBook;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testMatchOrders_SuccessfulMatch() {
        // Setup Buy Order
        Order buyOrder = new Order();
        buyOrder.setId("buy1");
        buyOrder.setUserId("buyer1");
        buyOrder.setType(Order.OrderType.BUY);
        buyOrder.setQuantity(10.0);
        buyOrder.setPricePerUnit(100.0);
        buyOrder.setStatus(Order.OrderStatus.PENDING);

        // Setup Sell Order
        Order sellOrder = new Order();
        sellOrder.setId("sell1");
        sellOrder.setUserId("seller1");
        sellOrder.setType(Order.OrderType.SELL);
        sellOrder.setQuantity(10.0);
        sellOrder.setPricePerUnit(90.0);
        sellOrder.setStatus(Order.OrderStatus.PENDING);

        // Mock OrderBook behavior
        when(orderBook.hasMatch()).thenReturn(true, false); // Match once, then stop
        when(orderBook.getBestBuy()).thenReturn(buyOrder);
        when(orderBook.getBestSell()).thenReturn(sellOrder);

        // Execute Matching
        orderMatchingService.matchOrders();

        // Verify Trade Creation
        verify(tradeRepository, times(1)).save(any(Trade.class));

        // Verify Wallet Updates via WalletService
        // Atomic settlement
        verify(walletService).processTrade("buyer1", "seller1", 10.0, 90.0);

        // Refund Buyer: (100 - 90) * 10 = 100
        verify(walletService).releaseFunds("buyer1", 100.0);

        // Verify Order Status Updates
        // Since we mock OrderBook, we need to verify that OrderMatchingService updates
        // the order objects
        // and calls orderRepository.save()
        verify(orderRepository, times(2)).save(any(Order.class));
    }

    @Test
    public void testMatchOrders_NoMatch() {
        when(orderBook.hasMatch()).thenReturn(false);

        orderMatchingService.matchOrders();

        verify(tradeRepository, never()).save(any(Trade.class));
    }
}
