package com.carboncredit.controller;

import com.carboncredit.model.CarbonCredit;
import com.carboncredit.model.Trade;
import com.carboncredit.repository.CarbonCreditRepository;
import com.carboncredit.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping({ "/api/pricing", "/pricing" })
public class PricingController {

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private CarbonCreditRepository carbonCreditRepository;

    // Fixed price model
    @GetMapping("/fixed-price")
    public double getFixedPrice(@RequestParam(defaultValue = "100.0") double basePrice) {
        return basePrice;
    }

    // Supply-demand based pricing model
    @GetMapping("/dynamic-price")
    public DynamicPriceResponse getDynamicPrice(
            @RequestParam(defaultValue = "100.0") double basePrice,
            @RequestParam(defaultValue = "0.1") double sensitivityFactor) {

        DynamicPriceResponse response = new DynamicPriceResponse();

        // Calculate total demand (credits needed by buyers)
        double totalDemand = calculateTotalDemand();

        // Calculate total supply (available credits for sale)
        double totalSupply = calculateTotalSupply();

        response.setBasePrice(basePrice);
        response.setTotalDemand(totalDemand);
        response.setTotalSupply(totalSupply);

        // Calculate dynamic price using the formula: P = P0 * (1 + α * (D/S - 1))
        if (totalSupply > 0) {
            double priceMultiplier = 1 + sensitivityFactor * (totalDemand / totalSupply - 1);
            response.setDynamicPrice(basePrice * priceMultiplier);
        } else {
            response.setDynamicPrice(basePrice);
        }

        return response;
    }

    private double calculateTotalDemand() {
        // In a real implementation, this would query open buy orders
        // For now, we'll use a simplified approach based on recent trades
        List<Trade> recentTrades = tradeRepository.findTop10ByOrderByExecutedAtDesc();
        return recentTrades.stream().mapToDouble(Trade::getQuantity).sum();
    }

    private double calculateTotalSupply() {
        // Calculate total available credits
        List<CarbonCredit> availableCredits = carbonCreditRepository.findByStatus(CarbonCredit.CreditStatus.VERIFIED);
        return availableCredits.stream().mapToDouble(CarbonCredit::getQuantity).sum();
    }

    // Calculate credits needed by a company
    @PostMapping("/credits-needed")
    public double calculateCreditsNeeded(@RequestBody CreditsCalculationRequest request) {
        return Math.max(0, request.getActualEmissions() - request.getAllowedLimit());
    }

    // Calculate extra credits for a company
    @PostMapping("/extra-credits")
    public double calculateExtraCredits(@RequestBody CreditsCalculationRequest request) {
        return Math.max(0, request.getAllowedLimit() - request.getActualEmissions());
    }

    // Calculate credits used by a company
    @PostMapping("/credits-used")
    public double calculateCreditsUsed(@RequestBody CreditsUsageRequest request) {
        return Math.min(request.getCreditsBought(), request.getCreditsNeeded());
    }

    // DTO classes
    public static class DynamicPriceResponse {
        private double basePrice;
        private double totalDemand;
        private double totalSupply;
        private double dynamicPrice;

        // Getters and setters
        public double getBasePrice() {
            return basePrice;
        }

        public void setBasePrice(double basePrice) {
            this.basePrice = basePrice;
        }

        public double getTotalDemand() {
            return totalDemand;
        }

        public void setTotalDemand(double totalDemand) {
            this.totalDemand = totalDemand;
        }

        public double getTotalSupply() {
            return totalSupply;
        }

        public void setTotalSupply(double totalSupply) {
            this.totalSupply = totalSupply;
        }

        public double getDynamicPrice() {
            return dynamicPrice;
        }

        public void setDynamicPrice(double dynamicPrice) {
            this.dynamicPrice = dynamicPrice;
        }
    }

    public static class CreditsCalculationRequest {
        private double actualEmissions;
        private double allowedLimit;

        // Getters and setters
        public double getActualEmissions() {
            return actualEmissions;
        }

        public void setActualEmissions(double actualEmissions) {
            this.actualEmissions = actualEmissions;
        }

        public double getAllowedLimit() {
            return allowedLimit;
        }

        public void setAllowedLimit(double allowedLimit) {
            this.allowedLimit = allowedLimit;
        }
    }

    public static class CreditsUsageRequest {
        private double creditsBought;
        private double creditsNeeded;

        // Getters and setters
        public double getCreditsBought() {
            return creditsBought;
        }

        public void setCreditsBought(double creditsBought) {
            this.creditsBought = creditsBought;
        }

        public double getCreditsNeeded() {
            return creditsNeeded;
        }

        public void setCreditsNeeded(double creditsNeeded) {
            this.creditsNeeded = creditsNeeded;
        }
    }
}