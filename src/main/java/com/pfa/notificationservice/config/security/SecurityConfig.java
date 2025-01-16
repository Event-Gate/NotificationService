package com.pfa.notificationservice.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration @EnableWebSecurity @RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
            SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA512");

            return NimbusJwtDecoder.withSecretKey(key)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        } catch (Exception e) {
            log.error("Failed to create JWT decoder", e);
            throw new RuntimeException(e);
        }
    }
}

