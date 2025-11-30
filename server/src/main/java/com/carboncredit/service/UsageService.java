package com.carboncredit.service;

import com.carboncredit.model.UsageConfig;
import com.carboncredit.model.UsageEntry;
import com.carboncredit.model.User;
import com.carboncredit.repository.UsageConfigRepository;
import com.carboncredit.repository.UsageEntryRepository;
import com.carboncredit.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageService {

    private final UsageEntryRepository usageEntryRepository;
    private final UsageConfigRepository usageConfigRepository;
    private final UserRepository userRepository;
    private final com.carboncredit.repository.WalletRepository walletRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public java.util.Map<String, Object> getUsageSummary(String userId) {
        Double totalUsageTons = getTotalUsage(userId); // Already in tons
        Double totalUsageKg = getTotalUsageInKg(userId);
        com.carboncredit.model.Wallet wallet = walletRepository.findByUserId(userId);
        Double walletCredits = (wallet != null) ? wallet.getCarbonCreditBalance() : 0.0;
        Double netRemaining = walletCredits - totalUsageTons; // Credits are in tons

        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("totalUsageKg", totalUsageKg);
        summary.put("totalUsageTons", totalUsageTons);
        summary.put("walletCredits", walletCredits);
        summary.put("netRemainingCredits", netRemaining);
        summary.put("showWarning", netRemaining < 0);
        return summary;
    }

    public UsageConfig getUsageConfig(String userId) {
        return usageConfigRepository.findByUserId(userId).orElse(null);
    }

    public UsageConfig saveUsageConfig(String userId, UsageConfig config) {
        Optional<UsageConfig> existing = usageConfigRepository.findByUserId(userId);
        if (existing.isPresent()) {
            UsageConfig current = existing.get();
            current.setBroker(config.getBroker());
            current.setClientId(config.getClientId());
            current.setUsername(config.getUsername());
            current.setPassword(config.getPassword());
            current.setTopic(config.getTopic());
            current.setUpdatedAt(LocalDateTime.now());
            return usageConfigRepository.save(current);
        } else {
            config.setUserId(userId);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            return usageConfigRepository.save(config);
        }
    }

    public List<UsageEntry> getRecentUsage(String userId) {
        return usageEntryRepository.findTop5ByUserIdOrderByTimestampDesc(userId);
    }

    public List<UsageEntry> getUsageHistory(String userId) {
        // Return all or a limited set for the chart. For now, let's return all.
        return usageEntryRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public Double getTotalUsage(String userId) {
        List<UsageEntry> entries = usageEntryRepository.findByUserIdOrderByTimestampDesc(userId);
        // Convert kg to tons (1000 kg = 1 ton = 1 credit)
        double totalKg = entries.stream().mapToDouble(UsageEntry::getCo2KgDelta).sum();
        return totalKg / 1000.0;
    }

    public Double getTotalUsageInKg(String userId) {
        List<UsageEntry> entries = usageEntryRepository.findByUserIdOrderByTimestampDesc(userId);
        return entries.stream().mapToDouble(UsageEntry::getCo2KgDelta).sum();
    }

    public void processMqttMessage(String payload, String knownUserId) {
        try {
            String userId = knownUserId;
            Double co2Delta = 0.0;
            LocalDateTime timestamp = LocalDateTime.now();

            if (payload.trim().startsWith("{")) {
                // Parse JSON
                JsonNode node = objectMapper.readTree(payload);
                // If userId is not provided (or we want to trust payload?), let's prefer
                // knownUserId if not null.
                // But if payload has a DIFFERENT userId, what do we do?
                // Let's assume knownUserId is the source of truth if provided.
                if (userId == null) {
                    if (node.has("userId")) {
                        userId = node.get("userId").asText();
                    } else if (node.has("email")) {
                        String email = node.get("email").asText();
                        Optional<User> user = userRepository.findByEmail(email);
                        if (user.isPresent()) {
                            userId = user.get().getId();
                        }
                    }
                }

                if (node.has("co2KgDelta")) {
                    co2Delta = node.get("co2KgDelta").asDouble();
                }

                if (node.has("timestamp")) {
                    try {
                        timestamp = LocalDateTime.parse(node.get("timestamp").asText(),
                                DateTimeFormatter.ISO_DATE_TIME);
                    } catch (Exception e) {
                        log.warn("Could not parse timestamp from payload, using current time");
                    }
                }
            } else {
                // Raw number
                try {
                    co2Delta = Double.parseDouble(payload);
                    if (userId == null) {
                        userId = "DEFAULT_USER";
                    }
                } catch (NumberFormatException e) {
                    log.error("Payload is not JSON and not a number: {}", payload);
                    return;
                }
            }

            if (userId != null) {
                UsageEntry entry = new UsageEntry();
                entry.setUserId(userId);
                entry.setCo2KgDelta(co2Delta);
                entry.setTimestamp(timestamp);
                entry.setRawPayload(payload);
                usageEntryRepository.save(entry);
                log.info("Saved usage entry for user {}: {} kg", userId, co2Delta);
            } else {
                log.warn("Could not resolve user for payload: {}", payload);
            }

        } catch (Exception e) {
            log.error("Error processing MQTT message", e);
        }
    }

    public void processMqttMessage(String payload) {
        processMqttMessage(payload, null);
    }
}
