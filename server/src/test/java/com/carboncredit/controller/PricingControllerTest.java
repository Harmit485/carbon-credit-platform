package com.carboncredit.controller;

import com.carboncredit.model.CarbonCredit;
import com.carboncredit.model.Trade;
import com.carboncredit.repository.CarbonCreditRepository;
import com.carboncredit.repository.TradeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PricingControllerTest {

    @InjectMocks
    private PricingController pricingController;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private CarbonCreditRepository carbonCreditRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFixedPrice() {
        double price = pricingController.getFixedPrice(150.0);
        assertEquals(150.0, price);
    }

    @Test
    public void testDynamicPrice_DemandExceedsSupply() {
        // Demand = 20 (from recent trades)
        Trade trade1 = new Trade();
        trade1.setQuantity(20.0);
        when(tradeRepository.findTop10ByOrderByExecutedAtDesc()).thenReturn(Arrays.asList(trade1));

        // Supply = 10 (available credits)
        CarbonCredit credit1 = new CarbonCredit();
        credit1.setQuantity(10.0);
        when(carbonCreditRepository.findByStatus(CarbonCredit.CreditStatus.VERIFIED))
                .thenReturn(Arrays.asList(credit1));

        // Base Price = 100, Sensitivity = 0.1
        // Formula: P = P0 * (1 + alpha * (D/S - 1))
        // P = 100 * (1 + 0.1 * (20/10 - 1))
        // P = 100 * (1 + 0.1 * 1) = 110

        PricingController.DynamicPriceResponse response = pricingController.getDynamicPrice(100.0, 0.1);

        assertEquals(20.0, response.getTotalDemand());
        assertEquals(10.0, response.getTotalSupply());
        assertEquals(110.0, response.getDynamicPrice(), 0.01);
    }

    @Test
    public void testDynamicPrice_SupplyExceedsDemand() {
        // Demand = 10
        Trade trade1 = new Trade();
        trade1.setQuantity(10.0);
        when(tradeRepository.findTop10ByOrderByExecutedAtDesc()).thenReturn(Arrays.asList(trade1));

        // Supply = 20
        CarbonCredit credit1 = new CarbonCredit();
        credit1.setQuantity(20.0);
        when(carbonCreditRepository.findByStatus(CarbonCredit.CreditStatus.VERIFIED))
                .thenReturn(Arrays.asList(credit1));

        // P = 100 * (1 + 0.1 * (10/20 - 1))
        // P = 100 * (1 + 0.1 * -0.5)
        // P = 100 * (1 - 0.05) = 95

        PricingController.DynamicPriceResponse response = pricingController.getDynamicPrice(100.0, 0.1);

        assertEquals(95.0, response.getDynamicPrice(), 0.01);
    }

    @Test
    public void testCreditsCalculations() {
        PricingController.CreditsCalculationRequest req = new PricingController.CreditsCalculationRequest();
        req.setActualEmissions(120);
        req.setAllowedLimit(100);

        assertEquals(20.0, pricingController.calculateCreditsNeeded(req));
        assertEquals(0.0, pricingController.calculateExtraCredits(req));

        req.setActualEmissions(80);
        assertEquals(0.0, pricingController.calculateCreditsNeeded(req));
        assertEquals(20.0, pricingController.calculateExtraCredits(req));
    }
}
