package com.carboncredit.controller;

import com.carboncredit.model.*;
import com.carboncredit.repository.*;
import com.carboncredit.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private VerificationRepository verificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarbonCreditRepository carbonCreditRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @PostMapping("/verify/{projectId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> verifyProject(@PathVariable String projectId,
            @RequestBody Map<String, String> requestBody) {
        String status = requestBody.get("status");

        return projectRepository.findById(projectId)
                .map(project -> {
                    // Update project status based on verification
                    switch (status) {
                        case "APPROVED":
                            project.setStatus(Project.ProjectStatus.VERIFIED);
                            break;
                        case "REJECTED":
                            project.setStatus(Project.ProjectStatus.REJECTED);
                            break;
                        default:
                            return ResponseEntity.badRequest().body("Invalid status: " + status);
                    }

                    // Save updated project
                    Project updatedProject = projectRepository.save(project);

                    // Create verification record
                    Verification verification = new Verification();
                    verification.setProjectId(projectId);

                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                    verification.setVerifierId(userDetails.getId());

                    verification.setStatus(Verification.VerificationStatus.valueOf(status));
                    verification.setCompletedAt(LocalDateTime.now());
                    verificationRepository.save(verification);

                    Map<String, Object> response = new HashMap<>();
                    response.put("project", updatedProject);
                    response.put("verification", verification);

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/verifications")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Verification> getAllVerifications() {
        return verificationRepository.findAll();
    }

    @GetMapping("/verifications/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Verification> getVerificationById(@PathVariable String id) {
        return verificationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // User management
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(userDetails.getName());
                    user.setEmail(userDetails.getEmail());
                    user.setOrganization(userDetails.getOrganization());
                    user.setCountry(userDetails.getCountry());
                    user.setRoles(userDetails.getRoles());
                    return ResponseEntity.ok(userRepository.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Credit management
    @PostMapping("/credits")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public CarbonCredit createCredit(@RequestBody CarbonCredit credit) {
        return carbonCreditRepository.save(credit);
    }

    @PutMapping("/credits/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CarbonCredit> updateCredit(@PathVariable String id, @RequestBody CarbonCredit creditDetails) {
        return carbonCreditRepository.findById(id)
                .map(credit -> {
                    credit.setProjectId(creditDetails.getProjectId());
                    credit.setQuantity(creditDetails.getQuantity());
                    credit.setVintageYear(creditDetails.getVintageYear());
                    credit.setOwnerId(creditDetails.getOwnerId());
                    credit.setStatus(creditDetails.getStatus());
                    credit.setPricePerUnit(creditDetails.getPricePerUnit());
                    return ResponseEntity.ok(carbonCreditRepository.save(credit));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/credits/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteCredit(@PathVariable String id) {
        return carbonCreditRepository.findById(id)
                .map(credit -> {
                    carbonCreditRepository.delete(credit);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // System statistics
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalProjects", projectRepository.count());
        stats.put("totalCredits", carbonCreditRepository.count());
        stats.put("totalTrades", tradeRepository.count());

        // Calculate average trade price
        List<Trade> allTrades = tradeRepository.findAll();
        double avgPrice = allTrades.stream()
                .mapToDouble(Trade::getPricePerUnit)
                .average()
                .orElse(0.0);
        stats.put("averageTradePrice", avgPrice);

        // Calculate Market Demand (Active Buy Orders)
        List<Order> buyOrders = orderRepository.findByTypeAndStatus(Order.OrderType.BUY, Order.OrderStatus.PENDING);
        buyOrders.addAll(orderRepository.findByTypeAndStatus(Order.OrderType.BUY, Order.OrderStatus.PARTIAL));
        double marketDemand = buyOrders.stream().mapToDouble(Order::getQuantity).sum();
        stats.put("marketDemand", marketDemand);

        // Calculate Market Supply (Active Sell Orders)
        List<Order> sellOrders = orderRepository.findByTypeAndStatus(Order.OrderType.SELL, Order.OrderStatus.PENDING);
        sellOrders.addAll(orderRepository.findByTypeAndStatus(Order.OrderType.SELL, Order.OrderStatus.PARTIAL));
        double marketSupply = sellOrders.stream().mapToDouble(Order::getQuantity).sum();
        stats.put("marketSupply", marketSupply);

        return ResponseEntity.ok(stats);
    }

    // Market Activity Endpoint
    @GetMapping("/market-activity")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> getMarketActivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        LocalDate endDate = (to != null) ? to : LocalDate.now();
        LocalDate startDate = (from != null) ? from : endDate.minusMonths(6);

        List<String> labels = new ArrayList<>();
        List<Long> tradesData = new ArrayList<>();
        List<Long> creditsData = new ArrayList<>();

        // Iterate by month
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime startOfMonth = current.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endOfMonth = current.withDayOfMonth(current.lengthOfMonth()).atTime(LocalTime.MAX);

            // Format label (e.g., "Jan 2023")
            labels.add(current.format(DateTimeFormatter.ofPattern("MMM yyyy")));

            // Count trades
            long tradesCount = tradeRepository.countByExecutedAtBetween(startOfMonth, endOfMonth);
            tradesData.add(tradesCount);

            // Count new credits
            long creditsCount = carbonCreditRepository.countByIssuedAtBetween(startOfMonth, endOfMonth);
            creditsData.add(creditsCount);

            current = current.plusMonths(1);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("labels", labels);
        response.put("trades", tradesData);
        response.put("newCredits", creditsData);

        return ResponseEntity.ok(response);
    }

    // Override records (with audit log)
    @PutMapping("/override/{entityType}/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> overrideRecord(@PathVariable String entityType, @PathVariable String id,
            @RequestBody Map<String, Object> updates) {
        try {
            switch (entityType.toLowerCase()) {
                case "project":
                    return updateProject(id, updates);
                case "credit":
                    return updateCreditRecord(id, updates);
                case "order":
                    return updateOrder(id, updates);
                default:
                    return ResponseEntity.badRequest().body("Unsupported entity type: " + entityType);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error overriding record: " + e.getMessage());
        }
    }

    private ResponseEntity<?> updateProject(String id, Map<String, Object> updates) {
        Optional<Project> projectOpt = projectRepository.findById(id);
        if (projectOpt.isPresent()) {
            Project project = projectOpt.get();
            // Update fields based on provided data
            if (updates.containsKey("name")) {
                project.setName((String) updates.get("name"));
            }
            if (updates.containsKey("description")) {
                project.setDescription((String) updates.get("description"));
            }
            if (updates.containsKey("status")) {
                project.setStatus(Project.ProjectStatus.valueOf((String) updates.get("status")));
            }
            return ResponseEntity.ok(projectRepository.save(project));
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<?> updateCreditRecord(String id, Map<String, Object> updates) {
        Optional<CarbonCredit> creditOpt = carbonCreditRepository.findById(id);
        if (creditOpt.isPresent()) {
            CarbonCredit credit = creditOpt.get();
            // Update fields based on provided data
            if (updates.containsKey("quantity")) {
                credit.setQuantity(((Number) updates.get("quantity")).doubleValue());
            }
            if (updates.containsKey("vintageYear")) {
                credit.setVintageYear(((Number) updates.get("vintageYear")).intValue());
            }
            if (updates.containsKey("status")) {
                credit.setStatus(CarbonCredit.CreditStatus.valueOf((String) updates.get("status")));
            }
            return ResponseEntity.ok(carbonCreditRepository.save(credit));
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<?> updateOrder(String id, Map<String, Object> updates) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            // Update fields based on provided data
            if (updates.containsKey("status")) {
                order.setStatus(Order.OrderStatus.valueOf((String) updates.get("status")));
            }
            if (updates.containsKey("quantity")) {
                order.setQuantity(((Number) updates.get("quantity")).doubleValue());
            }
            return ResponseEntity.ok(orderRepository.save(order));
        }
        return ResponseEntity.notFound().build();
    }
}