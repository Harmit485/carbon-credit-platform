package com.carboncredit.service;

import com.carboncredit.model.Order;
import com.carboncredit.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PricingService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private com.carboncredit.repository.TradeRepository tradeRepository;

    private static final double BASE_PRICE = 10000.0;
    private static final double ALPHA = 0.1; // Sensitivity factor

    /**
     * Gets the last traded price.
     * If no trades exist, returns the BASE_PRICE (10,000).
     */
    public double getLastTradedPrice() {
        return tradeRepository.findTopByOrderByExecutedAtDesc()
                .map(com.carboncredit.model.Trade::getPricePerUnit)
                .orElse(BASE_PRICE);
    }

    /**
     * Calculates the dynamic price based on Supply and Demand.
     * Formula: P = P0 * (1 + alpha * (D - S) / S)
     *
     * @param basePrice The base price (P0)
     * @return The calculated dynamic price
     */
    public double calculateDynamicPrice(double basePrice) {
        double demand = getTotalDemand();
        double supply = getTotalSupply();

        if (supply == 0) {
            return basePrice * (1 + ALPHA); // If no supply, price increases
        }

        return basePrice * (1 + ALPHA * (demand - supply) / supply);
    }

    private double getTotalDemand() {
        List<Order> buyOrders = orderRepository.findByTypeAndStatus(Order.OrderType.BUY, Order.OrderStatus.PENDING);
        return buyOrders.stream().mapToDouble(Order::getQuantity).sum();
    }

    private double getTotalSupply() {
        List<Order> sellOrders = orderRepository.findByTypeAndStatus(Order.OrderType.SELL, Order.OrderStatus.PENDING);
        return sellOrders.stream().mapToDouble(Order::getQuantity).sum();
    }

    // --- Utility Calculations ---

    /**
     * Calculates Credits Needed (CN)
     * CN = Emissions - Credits Owned
     */
    public double calculateCreditsNeeded(double totalEmissions, double creditsOwned) {
        return Math.max(0, totalEmissions - creditsOwned);
    }

    /**
     * Calculates Extra Credits (EC)
     * EC = Credits Owned - Emissions
     */
    public double calculateExtraCredits(double totalEmissions, double creditsOwned) {
        return Math.max(0, creditsOwned - totalEmissions);
    }

    /**
     * Calculates Credits Used (CU)
     * This is typically the amount retired or offset.
     */
    public double calculateCreditsUsed(double initialCredits, double currentCredits) {
        return Math.max(0, initialCredits - currentCredits);
    }
}
