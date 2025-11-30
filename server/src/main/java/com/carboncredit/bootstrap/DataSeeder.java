package com.carboncredit.bootstrap;

import com.carboncredit.model.*;
import com.carboncredit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CarbonCreditRepository carbonCreditRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            seedUsersAndWallets();
            seedCarbonCredits();
            seedOrders();
            seedTrades();
            System.out.println("Data seeding completed successfully!");
        }
    }

    private void seedUsersAndWallets() {
        createUser("buyer", "buyer@test.com", "password", User.Role.ROLE_USER);
        createUser("seller", "seller@test.com", "password", User.Role.ROLE_USER);
        createUser("admin", "admin@test.com", "password", User.Role.ROLE_ADMIN);
        createUser("Admin User", "admin@carboncredit.com", "Admin@123", User.Role.ROLE_ADMIN);
    }

    private void createUser(String name, String email, String password, User.Role roleEnum) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        Set<User.Role> roles = new HashSet<>();
        roles.add(roleEnum);
        user.setRoles(roles);

        userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(10000.00);
        walletRepository.save(wallet);
    }

    private void seedCarbonCredits() {
        User seller = userRepository.findByEmail("seller@test.com").orElseThrow();

        createCredit(seller.getId(), "Solar Project A", "Renewable Energy", 1000, 15.0,
                CarbonCredit.CreditStatus.ISSUED);
        createCredit(seller.getId(), "Wind Farm B", "Renewable Energy", 500, 12.5, CarbonCredit.CreditStatus.ISSUED);
        createCredit(seller.getId(), "Reforestation C", "Forestry", 2000, 20.0, CarbonCredit.CreditStatus.ISSUED);
    }

    private void createCredit(String ownerId, String source, String type, double quantity, double price,
            CarbonCredit.CreditStatus status) {
        CarbonCredit credit = new CarbonCredit();
        credit.setOwnerId(ownerId);
        credit.setProjectId(source);
        credit.setQuantity(quantity);
        credit.setPricePerUnit(price);
        credit.setStatus(status);
        credit.setIssuedAt(LocalDateTime.now());
        carbonCreditRepository.save(credit);
    }

    private void seedOrders() {
        User buyer = userRepository.findByEmail("buyer@test.com").orElseThrow();
        User seller = userRepository.findByEmail("seller@test.com").orElseThrow();
        List<CarbonCredit> credits = carbonCreditRepository.findAll();

        if (!credits.isEmpty()) {
            CarbonCredit credit = credits.get(0);

            // Buy Order
            Order buyOrder = new Order();
            buyOrder.setUserId(buyer.getId());
            buyOrder.setType(Order.OrderType.BUY);
            buyOrder.setQuantity(100);
            buyOrder.setPricePerUnit(14.5);
            buyOrder.setStatus(Order.OrderStatus.PENDING);
            buyOrder.setCreatedAt(LocalDateTime.now());
            orderRepository.save(buyOrder);

            // Sell Order
            Order sellOrder = new Order();
            sellOrder.setUserId(seller.getId());
            sellOrder.setType(Order.OrderType.SELL);
            sellOrder.setQuantity(50);
            sellOrder.setPricePerUnit(16.0);
            sellOrder.setStatus(Order.OrderStatus.PENDING);
            sellOrder.setCreatedAt(LocalDateTime.now());
            orderRepository.save(sellOrder);
        }
    }

    private void seedTrades() {
        User buyer = userRepository.findByEmail("buyer@test.com").orElseThrow();
        User seller = userRepository.findByEmail("seller@test.com").orElseThrow();
        List<CarbonCredit> credits = carbonCreditRepository.findAll();

        if (!credits.isEmpty()) {
            CarbonCredit credit = credits.get(0);

            // Create some historical trades
            createTrade(buyer.getId(), seller.getId(), credit.getId(), 10, 14.8, LocalDateTime.now().minusDays(2));
            createTrade(buyer.getId(), seller.getId(), credit.getId(), 20, 15.0, LocalDateTime.now().minusDays(1));
            createTrade(buyer.getId(), seller.getId(), credit.getId(), 15, 15.2, LocalDateTime.now().minusHours(5));
        }
    }

    private void createTrade(String buyerId, String sellerId, String creditId, double quantity, double price,
            LocalDateTime executedAt) {
        Trade trade = new Trade();
        trade.setBuyerId(buyerId);
        trade.setSellerId(sellerId);
        trade.setCreditId(creditId);
        trade.setQuantity(quantity);
        trade.setPricePerUnit(price);
        trade.setTotalAmount(quantity * price);
        trade.setExecutedAt(executedAt);
        tradeRepository.save(trade);
    }
}
