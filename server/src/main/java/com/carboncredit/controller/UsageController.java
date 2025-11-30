package com.carboncredit.controller;

import com.carboncredit.model.UsageConfig;
import com.carboncredit.model.UsageEntry;
import com.carboncredit.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@AuthenticationPrincipal UserDetails userDetails) {
        // We need to resolve userId from UserDetails.
        // Assuming UserDetails.getUsername() returns the email or username, we might
        // need to look up the user ID.
        // Or if we have a custom UserDetails implementation that has the ID.
        // For now, let's assume we can get the user ID from the service using the
        // username/email if needed,
        // but UsageService expects userId.
        // Let's assume we can get the user ID via a helper or repository lookup.
        // Actually, let's inject UserRepository here to resolve email -> userId if
        // needed.
        // But wait, the prompt says "Use the authenticated user from the JWT context".
        // Usually we have a way to get the User object.

        // Let's assume we can get the user ID. I'll add a helper method to resolve it.
        // For now, I'll assume the username in UserDetails is the email, and I'll look
        // up the user.
        // But I don't have UserRepository injected here.
        // I'll rely on UsageService to handle "get by email" or I'll inject
        // UserRepository.
        // Let's inject UserRepository.
        return ResponseEntity.ok(usageService.getUsageSummary(getUserId(userDetails)));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<UsageEntry>> getRecent(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usageService.getRecentUsage(getUserId(userDetails)));
    }

    @GetMapping("/history")
    public ResponseEntity<List<UsageEntry>> getHistory(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usageService.getUsageHistory(getUserId(userDetails)));
    }

    @GetMapping("/config")
    public ResponseEntity<UsageConfig> getConfig(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(usageService.getUsageConfig(getUserId(userDetails)));
    }

    @PostMapping("/config")
    public ResponseEntity<UsageConfig> updateConfig(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UsageConfig config) {
        return ResponseEntity.ok(usageService.saveUsageConfig(getUserId(userDetails), config));
    }

    // Helper to get userId.
    // I will inject UserRepository to find the user by email (username).
    private final com.carboncredit.repository.UserRepository userRepository;

    private String getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(com.carboncredit.model.User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
