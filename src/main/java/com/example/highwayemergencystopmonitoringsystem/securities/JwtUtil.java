package com.example.highwayemergencystopmonitoringsystem.securities;

import com.example.highwayemergencystopmonitoringsystem.configs.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final JwtConfig jwtConfig;

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        return generateToken(username);
    }

    public String generateToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtConfig.getExpirationMs()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception e) {
            log.error("Error getting username from token", e);
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return false;
        }
    }
}
