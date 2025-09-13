package com.photooptimizer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides health check endpoints for Railway deployment
 */
@RestController
public class HealthController {
    
    /**
     * Simple health check endpoint for debugging Railway deployment
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("application", "Photo Optimizer");
        health.put("version", "1.0.0");
        health.put("profile", System.getProperty("spring.profiles.active", "default"));
        return ResponseEntity.ok(health);
    }
    
    /**
     * Simple status endpoint for Railway debugging
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", System.currentTimeMillis());
        status.put("application", "Photo Optimizer");
        status.put("version", "1.0.0");
        status.put("profile", System.getProperty("spring.profiles.active", "default"));
        status.put("contextPath", System.getProperty("server.servlet.context-path", ""));
        status.put("port", System.getProperty("server.port", "8080"));
        return ResponseEntity.ok(status);
    }
    
    /**
     * Additional health check endpoint for Railway
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
