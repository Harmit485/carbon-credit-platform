package com.carboncredit.controller;

import com.carboncredit.model.*;
import com.carboncredit.repository.*;
import com.carboncredit.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CarbonCreditRepository carbonCreditRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private RetirementRepository retirementRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            DashboardStats stats = new DashboardStats();

            // Overall platform statistics
            stats.setTotalProjects(projectRepository.count());
            stats.setTotalCredits(carbonCreditRepository.count());
            stats.setTotalUsers(userRepository.count());
            stats.setTotalTrades((int) tradeRepository.count());
            stats.setTotalRetirements((int) retirementRepository.count());

            // Credits by status - REMOVED
            // Map<String, Long> creditsByStatus = new HashMap<>();
            // for (CarbonCredit.CreditStatus status : CarbonCredit.CreditStatus.values()) {
            // creditsByStatus.put(status.name(),
            // carbonCreditRepository.countByStatus(status));
            // }
            // stats.setCreditsByStatus(creditsByStatus);

            // Recent trades
            List<Trade> recentTrades = tradeRepository.findTop10ByOrderByExecutedAtDesc();
            stats.setRecentTrades(recentTrades);

            // Top projects by credits issued - REMOVED
            // List<Project> projects = projectRepository.findAll();
            // Map<String, Double> topProjects = projects.stream()
            // .filter(p -> p.getName() != null) // Filter out null names
            // .collect(Collectors.toMap(
            // Project::getName,
            // p -> carbonCreditRepository.findByProjectId(p.getId()).stream()
            // .mapToDouble(CarbonCredit::getQuantity)
            // .sum(),
            // Double::sum)); // Merge function for duplicate keys
            // stats.setTopProjectsByCredits(topProjects);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching dashboard stats: " + e.getMessage());
        }
    }

    @GetMapping("/user-stats")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getUserDashboardStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String userId = userDetails.getId();
            UserDashboardStats stats = new UserDashboardStats();

            // User's wallet balance
            Wallet wallet = walletRepository.findByUserId(userId);
            if (wallet != null) {
                stats.setCreditBalance(wallet.getCarbonCreditBalance());
            } else {
                stats.setCreditBalance(0.0);
            }

            // User's projects (if issuer)
            List<Project> userProjects = projectRepository.findByIssuerId(userId);
            stats.setUserProjectsCount(userProjects.size());

            // User's credits
            List<CarbonCredit> userCredits = carbonCreditRepository.findByOwnerId(userId);
            stats.setUserCreditsCount(userCredits.size());

            // User's trades
            List<Trade> userTrades = new ArrayList<>();
            userTrades.addAll(tradeRepository.findByBuyerId(userId));
            userTrades.addAll(tradeRepository.findBySellerId(userId));
            stats.setUserTradesCount(userTrades.size());

            // User's retirements
            List<Retirement> userRetirements = retirementRepository.findByUserId(userId);
            stats.setUserRetirementsCount(userRetirements.size());

            // Recent user activity
            List<Order> userOrders = orderRepository.findByUserId(userId);
            stats.setRecentOrders(userOrders.stream()
                    .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                    .limit(5)
                    .collect(Collectors.toList()));

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error fetching user dashboard stats: " + e.getMessage());
        }
    }

    // @GetMapping("/market-data") - REMOVED
    // public ResponseEntity<?> getMarketData() { ... }

    // DTO classes for dashboard responses
    public static class DashboardStats {
        private long totalProjects;
        private long totalCredits;
        private long totalUsers;
        private int totalTrades;
        private int totalRetirements;
        private List<Trade> recentTrades;
        // private Map<String, Long> creditsByStatus; // REMOVED
        // private Map<String, Double> topProjectsByCredits; // REMOVED

        // Getters and setters
        public long getTotalProjects() {
            return totalProjects;
        }

        public void setTotalProjects(long totalProjects) {
            this.totalProjects = totalProjects;
        }

        public long getTotalCredits() {
            return totalCredits;
        }

        public void setTotalCredits(long totalCredits) {
            this.totalCredits = totalCredits;
        }

        public long getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(long totalUsers) {
            this.totalUsers = totalUsers;
        }

        public int getTotalTrades() {
            return totalTrades;
        }

        public void setTotalTrades(int totalTrades) {
            this.totalTrades = totalTrades;
        }

        public int getTotalRetirements() {
            return totalRetirements;
        }

        public void setTotalRetirements(int totalRetirements) {
            this.totalRetirements = totalRetirements;
        }

        public List<Trade> getRecentTrades() {
            return recentTrades;
        }

        public void setRecentTrades(List<Trade> recentTrades) {
            this.recentTrades = recentTrades;
        }

        // Getters and setters for removed fields commented out
        // public Map<String, Long> getCreditsByStatus() { return creditsByStatus; }
        // public void setCreditsByStatus(Map<String, Long> creditsByStatus) {
        // this.creditsByStatus = creditsByStatus; }
        // public Map<String, Double> getTopProjectsByCredits() { return
        // topProjectsByCredits; }
        // public void setTopProjectsByCredits(Map<String, Double> topProjectsByCredits)
        // { this.topProjectsByCredits = topProjectsByCredits; }
    }

    public static class UserDashboardStats {
        private double creditBalance;
        private int userProjectsCount;
        private int userCreditsCount;
        private int userTradesCount;
        private int userRetirementsCount;
        private List<Order> recentOrders;

        // Getters and setters
        public double getCreditBalance() {
            return creditBalance;
        }

        public void setCreditBalance(double creditBalance) {
            this.creditBalance = creditBalance;
        }

        public int getUserProjectsCount() {
            return userProjectsCount;
        }

        public void setUserProjectsCount(int userProjectsCount) {
            this.userProjectsCount = userProjectsCount;
        }

        public int getUserCreditsCount() {
            return userCreditsCount;
        }

        public void setUserCreditsCount(int userCreditsCount) {
            this.userCreditsCount = userCreditsCount;
        }

        public int getUserTradesCount() {
            return userTradesCount;
        }

        public void setUserTradesCount(int userTradesCount) {
            this.userTradesCount = userTradesCount;
        }

        public int getUserRetirementsCount() {
            return userRetirementsCount;
        }

        public void setUserRetirementsCount(int userRetirementsCount) {
            this.userRetirementsCount = userRetirementsCount;
        }

        public List<Order> getRecentOrders() {
            return recentOrders;
        }

        public void setRecentOrders(List<Order> recentOrders) {
            this.recentOrders = recentOrders;
        }
    }

    // MarketData class removed
}