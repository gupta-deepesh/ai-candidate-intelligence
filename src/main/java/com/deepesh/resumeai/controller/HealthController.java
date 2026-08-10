package com.deepesh.resumeai.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Value("${app.ai.mock:true}")
    private boolean mockMode;

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "application", "AI Resume Analyzer", "mockMode", mockMode, "timestamp", Instant.now().toString());
    }
}
