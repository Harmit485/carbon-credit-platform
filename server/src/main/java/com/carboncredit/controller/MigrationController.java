package com.carboncredit.controller;

import com.carboncredit.model.User;
import com.carboncredit.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MigrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private CarbonCreditRepository carbonCreditRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DeleteMapping("/reset-database")
    public ResponseEntity<?> resetDatabase() {
        try {
            // Delete all data
            tradeRepository.deleteAll();
            orderRepository.deleteAll();
            carbonCreditRepository.deleteAll();
            walletRepository.deleteAll();
            projectRepository.deleteAll();
            userRepository.deleteAll();

            // Create new users with correct roles
            createUser("buyer", "buyer@test.com", "password", User.Role.ROLE_USER);
            createUser("seller", "seller@test.com", "password", User.Role.ROLE_USER);
            createUser("admin", "admin@test.com", "password", User.Role.ROLE_ADMIN);

            return ResponseEntity.ok("Database reset successfully. New users created.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
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

        com.carboncredit.model.Wallet wallet = new com.carboncredit.model.Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(10000.00);
        wallet.setCarbonCreditBalance(100.0);
        walletRepository.save(wallet);
    }
}
