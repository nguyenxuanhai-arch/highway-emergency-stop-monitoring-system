package com.example.highwayemergencystopmonitoringsystem.configs;

import com.example.highwayemergencystopmonitoringsystem.securities.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers("/", "/login.html", "/register.html").permitAll()
                        .requestMatchers("/dashboard", "/dashboard.html").permitAll()
                        // Incident pages (authentication handled by JS)
                        .requestMatchers("/incidents/**").permitAll()
                        // Map page
                        .requestMatchers("/map").permitAll()
                        // Reports pages
                        .requestMatchers("/reports/**").permitAll()
                        // Settings page
                        .requestMatchers("/settings").permitAll()
                        // Auth API
                        .requestMatchers("/api/auth/**").permitAll()
                        // WebSocket
                        .requestMatchers("/ws", "/ws/**").permitAll()
                        // Image serving
                        .requestMatchers("/api/incidents/image/**").permitAll()
                        // Static resources and favicon
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", "/favicon.ico").permitAll()
                        .requestMatchers("/adminlte/**", "/plugins/**").permitAll()
                        // Protected API endpoints - require authentication
                        .requestMatchers("/api/incidents", "/api/incidents/**").authenticated()
                        .requestMatchers("/api/reports/**").authenticated()
                        .requestMatchers("/api/dashboard/**").authenticated()
                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
