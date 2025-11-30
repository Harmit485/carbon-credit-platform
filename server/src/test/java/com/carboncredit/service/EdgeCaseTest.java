package com.carboncredit.service;

import com.carboncredit.model.Order;
import com.carboncredit.model.Project;
import com.carboncredit.model.Wallet;
import com.carboncredit.repository.OrderRepository;
import com.carboncredit.repository.ProjectRepository;
import com.carboncredit.repository.TradeRepository;
import com.carboncredit.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class EdgeCaseTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private OrderMatchingService orderMatchingService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @BeforeEach
    public void setup() {
        walletRepository.deleteAll();
        orderRepository.deleteAll();
        tradeRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    public void testNegativeBalanceUpdate() {
        String userId = "negativeUser";
        walletService.createWalletIfNotExists(userId);

        // Try to subtract more than balance
        Exception exception = assertThrows(RuntimeException.class, () -> {
            walletService.updateBalance(userId, -100.0, 0.0);
        });

        assertTrue(exception.getMessage().contains("Wallet update failed"));
    }

    @Test
    public void testSelfTrading() {
        String userId = "selfTrader";

        // Setup Wallet
        walletService.createWalletIfNotExists(userId);
        walletService.updateBalance(userId, 1000.0, 100.0); // Money and Credits

        // Place Sell Order
        Order sellOrder = new Order();
        sellOrder.setUserId(userId);
        sellOrder.setType(Order.OrderType.SELL);
        sellOrder.setQuantity(10.0);
        sellOrder.setPricePerUnit(10.0);
        sellOrder.setStatus(Order.OrderStatus.PENDING);
        sellOrder.setCreatedAt(LocalDateTime.now());
        orderRepository.save(sellOrder);

        // Place Buy Order (Matches Sell Order)
        Order buyOrder = new Order();
        buyOrder.setUserId(userId);
        buyOrder.setType(Order.OrderType.BUY);
        buyOrder.setQuantity(10.0);
        buyOrder.setPricePerUnit(10.0);
        buyOrder.setStatus(Order.OrderStatus.PENDING);
        buyOrder.setCreatedAt(LocalDateTime.now().plusSeconds(1));
        orderRepository.save(buyOrder);

        // Run Matching
        orderMatchingService.matchOrders();

        // Verify NO Trade occurred
        assertEquals(0, tradeRepository.count());

        // Verify Orders are still PENDING
        List<Order> orders = orderRepository.findAll();
        for (Order order : orders) {
            assertEquals(Order.OrderStatus.PENDING, order.getStatus());
        }
    }

    @Test
    public void testPrecisionHandling() {
        String userId = "precisionUser";
        walletService.createWalletIfNotExists(userId);

        // Add 0.1 + 0.2. In float this is 0.30000000000000004
        walletService.updateBalance(userId, 0.1, 0.0);
        walletService.updateBalance(userId, 0.2, 0.0);

        Wallet wallet = walletRepository.findByUserId(userId);
        // We expect it to be approximately 0.3
        assertEquals(0.3, wallet.getBalance(), 0.000001);

        // Try to withdraw 0.3
        // If precision is off, this might fail if balance is slightly less than 0.3
        // (unlikely with addition, but possible with subtraction)
        // Actually, 0.1 + 0.2 is slightly LARGER than 0.3 in double.
        // So withdrawing 0.3 should succeed.

        walletService.updateBalance(userId, -0.3, 0.0);

        wallet = walletRepository.findByUserId(userId);
        assertEquals(0.0, wallet.getBalance(), 0.000001);
    }

    @Test
    public void testGenerateCreditsUpdatesWalletBalance() {
        String userId = "creditUser";

        // Create a reducing project owned by user
        Project project = new Project();
        project.setIssuerId(userId);
        project.setType(Project.ProjectType.SOLAR);
        project.setTotalCarbonCredits(100.0);
        project = projectRepository.save(project);

        // Generate credits via service
        creditService.generateCredits(project.getId(), 100.0, userId);

        Wallet wallet = walletRepository.findByUserId(userId);
        assertNotNull(wallet);
        assertEquals(100.0, wallet.getCarbonCreditBalance(), 0.000001);
    }

    @Test
    public void testConsumeCreditsUpdatesWalletBalance() {
        String userId = "consumeUser";

        // Create a producing project owned by user
        Project project = new Project();
        project.setIssuerId(userId);
        project.setType(Project.ProjectType.MANUFACTURING);
        project.setTotalCarbonCredits(200.0);
        project = projectRepository.save(project);

        // Seed wallet with credits
        walletService.createWalletIfNotExists(userId);
        walletService.updateBalance(userId, 0.0, 200.0);

        // Consume some credits
        creditService.consumeCredits(project.getId(), 50.0, userId);

        Wallet wallet = walletRepository.findByUserId(userId);
        assertNotNull(wallet);
        assertEquals(150.0, wallet.getCarbonCreditBalance(), 0.000001);
    }
}
