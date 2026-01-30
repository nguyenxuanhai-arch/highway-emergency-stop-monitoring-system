package com.example.highwayemergencystopmonitoringsystem.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    private String secret = "your-secret-key-min-256-bits-long-change-in-production";
    private Long expirationMs = 86400000L; // 24 hours
}

