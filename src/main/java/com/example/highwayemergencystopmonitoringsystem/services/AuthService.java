package com.example.highwayemergencystopmonitoringsystem.services;

import com.example.highwayemergencystopmonitoringsystem.dtos.AuthRequest;
import com.example.highwayemergencystopmonitoringsystem.dtos.AuthResponse;
import com.example.highwayemergencystopmonitoringsystem.entities.User;
import com.example.highwayemergencystopmonitoringsystem.repositories.UserRepository;
import com.example.highwayemergencystopmonitoringsystem.securities.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /**
     * Register new user (highway operator)
     */
    public AuthResponse register(AuthRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("User already exists with this email");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .userId(user.getId())
                .build();
    }

    /**
     * Login user and return JWT token
     */
    public AuthResponse login(AuthRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String token = jwtUtil.generateToken(authentication);

            return AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .userId(user.getId())
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }
    }
}
