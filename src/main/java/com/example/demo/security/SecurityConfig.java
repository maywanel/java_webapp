package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // For a REST API using JWT you usually disable CSRF (enable in browsers for forms)
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // allow unauthenticated access to your auth API and common static paths
                .requestMatchers("/api/auth/**", "/", "/index.html", "/static/**", "/css/**", "/js/**", "/favicon.ico").permitAll()
                .anyRequest().permitAll() // <-- allow all requests in dev
            );
            // httpBasic removed for open access in development

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
