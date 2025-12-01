package com.carboncredit.controller;

import com.carboncredit.model.Order;
import com.carboncredit.model.Trade;
import com.carboncredit.repository.OrderRepository;
import com.carboncredit.repository.TradeRepository;
import com.carboncredit.service.OrderMatchingService;
import com.carboncredit.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping({ "/api/marketplace", "/marketplace" })
public class MarketplaceController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private OrderMatchingService orderMatchingService;

    @Autowired
    private PricingService pricingService;

    @Autowired
    private com.carboncredit.service.WalletService walletService;

    // --- Orders (Buy & Sell) ---

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        // Return all active orders with userId for frontend filtering
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.PENDING
                        || order.getStatus() == Order.OrderStatus.PARTIAL)
                .collect(Collectors.toList());
    }

    @GetMapping("/orders/buy")
    public List<Order> getBuyOrders() {
        return orderRepository.findByTypeAndStatus(Order.OrderType.BUY, Order.OrderStatus.PENDING).stream()
                .map(this::anonymizeOrder)
                .collect(Collectors.toList());
    }

    @GetMapping("/orders/sell")
    public List<Order> getSellOrders() {
        return orderRepository.findByTypeAndStatus(Order.OrderType.SELL, Order.OrderStatus.PENDING).stream()
                .map(this::anonymizeOrder)
                .collect(Collectors.toList());
    }

    @GetMapping("/orders/user/{userId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Order> getUserOrders(@PathVariable String userId) {
        return orderRepository.findByUserId(userId);
    }

    @GetMapping("/orders/my")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public List<Order> getMyOrders() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        com.carboncredit.security.UserDetailsImpl userDetails = (com.carboncredit.security.UserDetailsImpl) authentication
                .getPrincipal();
        return orderRepository.findByUserId(userDetails.getId());
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Transactional
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        com.carboncredit.security.UserDetailsImpl userDetails = (com.carboncredit.security.UserDetailsImpl) authentication
                .getPrincipal();
        order.setUserId(userDetails.getId());

        // Validate order type
        if (order.getType() == null) {
            return ResponseEntity.badRequest().body("Order type is required.");
        }

        // Validate price range (Dynamic Pricing Rule)
        double lastTradedPrice = pricingService.getLastTradedPrice();
        double minPrice = lastTradedPrice * 0.9;
        double maxPrice = lastTradedPrice * 1.1;

        if (order.getPricePerUnit() < minPrice || order.getPricePerUnit() > maxPrice) {
            return ResponseEntity.badRequest().body(String.format(
                    "Price must be within ±10%% of the last traded price (%.2f). Allowed range: %.2f - %.2f",
                    lastTradedPrice, minPrice, maxPrice));
        }

        try {
            if (order.getType() == Order.OrderType.SELL) {
                // LOCK CREDITS: Reserve credits in Wallet
                System.out.println("Locking credits for SELL order. User: " + userDetails.getId() + ", Qty: "
                        + order.getQuantity());
                walletService.reserveCredits(userDetails.getId(), order.getQuantity());
            } else if (order.getType() == Order.OrderType.BUY) {
                // LOCK FUNDS: Reserve funds in Wallet
                double totalCost = order.getQuantity() * order.getPricePerUnit();
                System.out
                        .println("Locking funds for BUY order. User: " + userDetails.getId() + ", Cost: " + totalCost);
                walletService.reserveFunds(userDetails.getId(), totalCost);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Insufficient funds or credits to place order.");
        }

        order.setStatus(Order.OrderStatus.PENDING);
        order.setTotalAmount(order.getQuantity() * order.getPricePerUnit());
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Add to In-Memory Order Book and Trigger Matching
        orderMatchingService.addOrder(savedOrder);

        return ResponseEntity.ok(savedOrder);
    }

    @PutMapping("/orders/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @Transactional
    public ResponseEntity<?> cancelOrder(@PathVariable String id) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        com.carboncredit.security.UserDetailsImpl userDetails = (com.carboncredit.security.UserDetailsImpl) authentication
                .getPrincipal();
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return orderRepository.findById(id)
                .map(order -> {
                    if (!order.getUserId().equals(userDetails.getId()) && !isAdmin) {
                        return ResponseEntity.status(403).body("Unauthorized to cancel this order");
                    }
                    if (order.getStatus() == Order.OrderStatus.EXECUTED
                            || order.getStatus() == Order.OrderStatus.CANCELLED) {
                        return ResponseEntity.badRequest().body("Order cannot be cancelled");
                    }

                    // Remove from Order Book
                    orderMatchingService.cancelOrder(order.getId());

                    // REFUND LOGIC (Unlock)
                    if (order.getType() == Order.OrderType.BUY) {
                        // Unlock the remaining value
                        double refundAmount = order.getQuantity() * order.getPricePerUnit();
                        walletService.releaseFunds(order.getUserId(), refundAmount);
                    } else if (order.getType() == Order.OrderType.SELL) {
                        // Unlock the remaining quantity
                        walletService.releaseCredits(order.getUserId(), order.getQuantity());
                    }

                    order.setStatus(Order.OrderStatus.CANCELLED);
                    order.setCompletedAt(LocalDateTime.now());
                    return ResponseEntity.ok(orderRepository.save(order));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Trades ---

    @GetMapping("/trades")
    public List<Trade> getAllTrades() {
        return tradeRepository.findAll();
    }

    @GetMapping("/trades/buyer/{buyerId}")
    public List<Trade> getBuyerTrades(@PathVariable String buyerId) {
        return tradeRepository.findByBuyerId(buyerId);
    }

    @GetMapping("/trades/seller/{sellerId}")
    public List<Trade> getSellerTrades(@PathVariable String sellerId) {
        return tradeRepository.findBySellerId(sellerId);
    }

    // Get trades for the authenticated user (both as buyer and seller)
    @GetMapping("/trades/my")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public List<Trade> getMyTrades(@RequestParam(required = false) Integer minutes) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        com.carboncredit.security.UserDetailsImpl userDetails = (com.carboncredit.security.UserDetailsImpl) authentication
                .getPrincipal();
        String userId = userDetails.getId();

        List<Trade> allUserTrades = new java.util.ArrayList<>();
        allUserTrades.addAll(tradeRepository.findByBuyerId(userId));
        allUserTrades.addAll(tradeRepository.findBySellerId(userId));

        // Remove duplicates and filter by time if specified
        List<Trade> uniqueTrades = allUserTrades.stream()
                .distinct()
                .collect(Collectors.toList());

        if (minutes != null && minutes > 0) {
            LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(minutes);
            uniqueTrades = uniqueTrades.stream()
                    .filter(trade -> trade.getExecutedAt().isAfter(cutoffTime))
                    .collect(Collectors.toList());
        }

        // Sort by execution time, most recent first
        uniqueTrades.sort((t1, t2) -> t2.getExecutedAt().compareTo(t1.getExecutedAt()));

        return uniqueTrades;
    }

    @GetMapping("/price-history")
    public List<Trade> getPriceHistory() {
        // Return all trades sorted by execution time (oldest first) for the chart
        return tradeRepository.findAll().stream()
                .sorted((t1, t2) -> t1.getExecutedAt().compareTo(t2.getExecutedAt()))
                .collect(Collectors.toList());
    }

    // --- Pricing ---

    @GetMapping("/price/dynamic")
    public ResponseEntity<Double> getDynamicPrice(@RequestParam double basePrice) {
        return ResponseEntity.ok(pricingService.calculateDynamicPrice(basePrice));
    }

    // --- Admin ---

    @PostMapping("/match")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> triggerMatching() {
        try {
            orderMatchingService.matchOrders();
            return ResponseEntity.ok("Order matching triggered successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error matching orders: " + e.getMessage());
        }
    }

    private Order anonymizeOrder(Order order) {
        Order safeOrder = new Order();
        safeOrder.setId(order.getId());
        safeOrder.setType(order.getType());
        safeOrder.setQuantity(order.getQuantity());
        safeOrder.setPricePerUnit(order.getPricePerUnit());
        safeOrder.setStatus(order.getStatus());
        safeOrder.setCreatedAt(order.getCreatedAt());
        // Do NOT set userId
        return safeOrder;
    }
}