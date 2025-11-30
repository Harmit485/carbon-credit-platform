package com.carboncredit.service;

import com.carboncredit.model.UsageConfig;
import com.carboncredit.repository.UsageConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class MQTTSubscriberService {

    private final UsageConfigRepository usageConfigRepository;
    private final UsageService usageService;

    // Map userId -> MqttClient
    private final Map<String, IMqttClient> clients = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing MQTT Subscribers...");
        List<UsageConfig> configs = usageConfigRepository.findAll();
        for (UsageConfig config : configs) {
            connect(config);
        }
    }

    public void connect(UsageConfig config) {
        if (config == null || config.getUserId() == null)
            return;

        String userId = config.getUserId();
        disconnect(userId); // Disconnect existing if any

        try {
            String broker = config.getBroker();
            // Auto-generate client ID with constant prefix
            String clientId = "spring-boot-client-" + userId;

            MqttClient client = new MqttClient(broker, clientId, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                options.setUserName(config.getUsername());
            }
            if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                options.setPassword(config.getPassword().toCharArray());
            }

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("Connection lost for user {}: {}", userId, cause.getMessage());
                    // Optional: Implement reconnect logic
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String payload = new String(message.getPayload());
                    log.info("Message received for user {}: {}", userId, payload);
                    // We pass the payload to UsageService.
                    // Note: UsageService.processMqttMessage tries to parse userId from payload.
                    // But here we know the userId from the config context.
                    // However, the payload might be generic.
                    // We should probably enrich the payload or handle it.
                    // For now, we just pass it. UsageService handles "raw number" by assigning to
                    // DEFAULT_USER.
                    // If we know the userId, we should probably pass it explicitly?
                    // The prompt says: "The ESP32 payload includes a userId... or There is a simple
                    // mapping logic".
                    // If we are connected to THIS user's broker, we can assume the message is for
                    // THIS user.
                    // So let's overload processMqttMessage to accept userId.
                    usageService.processMqttMessage(payload, userId);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });

            client.connect(options);
            client.subscribe(config.getTopic());
            clients.put(userId, client);
            log.info("Connected to MQTT broker for user {}", userId);

        } catch (MqttException e) {
            log.error("Failed to connect to MQTT broker for user {}: {}", userId, e.getMessage());
        }
    }

    public void disconnect(String userId) {
        IMqttClient client = clients.remove(userId);
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
                client.close();
            } catch (MqttException e) {
                log.error("Error disconnecting client for user {}: {}", userId, e.getMessage());
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        for (String userId : clients.keySet()) {
            disconnect(userId);
        }
    }
}
