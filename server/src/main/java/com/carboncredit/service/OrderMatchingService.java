package com.carboncredit.service;

import com.carboncredit.model.Order;
import com.carboncredit.model.Trade;
import com.carboncredit.repository.OrderRepository;
import com.carboncredit.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

@Service
public class OrderMatchingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private OrderBook orderBook;

    @PostConstruct
    public void loadOrders() {
        System.out.println("Loading pending orders into memory...");
        orderBook.clear();
        List<Order> pendingOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING || o.getStatus() == Order.OrderStatus.PARTIAL)
                .collect(Collectors.toList());

        for (Order order : pendingOrders) {
            orderBook.addOrder(order);
        }
        System.out.println("Loaded " + pendingOrders.size() + " orders.");
        matchOrders();
    }

    public void addOrder(Order order) {
        orderBook.addOrder(order);
        matchOrders();
    }

    public void cancelOrder(String orderId) {
        orderBook.removeOrder(orderId);
    }

    @Transactional
    public synchronized void matchOrders() {
        while (orderBook.hasMatch()) {
            Order buyOrder = orderBook.getBestBuy();
            Order sellOrder = orderBook.getBestSell();

            if (buyOrder == null || sellOrder == null)
                break;

            // Double check price condition (redundant but safe)
            if (buyOrder.getPricePerUnit() < sellOrder.getPricePerUnit())
                break;

            // Skip self-trading
            if (buyOrder.getUserId().equals(sellOrder.getUserId())) {
                System.out.println("Self-trade detected. Cancelling newer order for user: " + buyOrder.getUserId());

                Order orderToCancel;
                // Cancel the newer order to unblock the book
                if (buyOrder.getCreatedAt().compareTo(sellOrder.getCreatedAt()) > 0) {
                    orderToCancel = buyOrder;
                } else {
                    orderToCancel = sellOrder;
                }

                // Cancel logic
                orderToCancel.setStatus(Order.OrderStatus.CANCELLED);
                orderToCancel.setCompletedAt(LocalDateTime.now());
                orderRepository.save(orderToCancel);
                orderBook.removeOrder(orderToCancel.getId());

                // Refund logic
                if (orderToCancel.getType() == Order.OrderType.BUY) {
                    walletService.releaseFunds(orderToCancel.getUserId(),
                            orderToCancel.getQuantity() * orderToCancel.getPricePerUnit());
                } else {
                    walletService.releaseCredits(orderToCancel.getUserId(), orderToCancel.getQuantity());
                }

                continue;
            }

            double matchQuantity = Math.min(buyOrder.getQuantity(), sellOrder.getQuantity());
            double tradePrice = sellOrder.getPricePerUnit(); // Execute at Maker (Sell) Price

            executeTrade(buyOrder, sellOrder, matchQuantity, tradePrice);

            // Update Orders
            updateOrder(buyOrder, matchQuantity);
            updateOrder(sellOrder, matchQuantity);
        }
    }

    private void updateOrder(Order order, double matchQuantity) {
        order.setQuantity(order.getQuantity() - matchQuantity);
        if (order.getQuantity() <= 0.0001) { // Epsilon for float comparison
            order.setQuantity(0);
            order.setStatus(Order.OrderStatus.EXECUTED);
            order.setCompletedAt(LocalDateTime.now());
            orderBook.removeOrder(order.getId());
        } else {
            order.setStatus(Order.OrderStatus.PARTIAL);
            // Order remains in book
        }
        orderRepository.save(order);
    }

    private void executeTrade(Order buyOrder, Order sellOrder, double quantity, double price) {
        // Create Trade Record
        Trade trade = new Trade();
        trade.setBuyerId(buyOrder.getUserId());
        trade.setSellerId(sellOrder.getUserId());
        trade.setQuantity(quantity);
        trade.setPricePerUnit(price);
        trade.setTotalAmount(quantity * price);
        trade.setExecutedAt(LocalDateTime.now());
        tradeRepository.save(trade);

        System.out.println("Executing trade. Buyer: " + buyOrder.getUserId() + ", Seller: " + sellOrder.getUserId()
                + ", Qty: " + quantity + ", Price: " + price);

        // Atomic Settlement
        walletService.processTrade(buyOrder.getUserId(), sellOrder.getUserId(), quantity, price);

        // Refund Buyer if Bid Price > Trade Price
        if (buyOrder.getPricePerUnit() > price) {
            double refundAmount = (buyOrder.getPricePerUnit() - price) * quantity;
            if (refundAmount > 0) {
                System.out.println("Refunding buyer excess: " + refundAmount);
                walletService.releaseFunds(buyOrder.getUserId(), refundAmount);
            }
        }
    }
}
