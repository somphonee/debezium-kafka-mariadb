package space.learn.co.la.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Kafka CDC Consumer");
        response.put("status", "running");
        response.put("timestamp", LocalDateTime.now());
        response.put("description", "Spring Boot consumer for Debezium CDC events");
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());

        // Check Kafka connectivity
        try {
            if (kafkaTemplate != null) {
                // This will throw an exception if Kafka is not reachable
                kafkaTemplate.partitionsFor("mariadb01.appdb.customers");
                response.put("kafka", "Connected");
            } else {
                response.put("kafka", "Not configured");
            }
        } catch (Exception e) {
            response.put("kafka", "Disconnected: " + e.getMessage());
            response.put("status", "DEGRADED");
        }

        return response;
    }
}
