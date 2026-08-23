package com.agent.ai_rag_chatbot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authEntryPoint) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/chatbot/rag/api/documents/**"
                        ).authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(basic ->
                        basic.authenticationEntryPoint(authEntryPoint));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.security.upload-username}") String username,
            @Value("${app.security.upload-password}") String password,
            PasswordEncoder encoder) {

        var user = User.builder()
                .username(username)
                .password(encoder.encode(password))
                .roles("DOCUMENT_UPLOADER")
                .build();

        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(
            ObjectMapper objectMapper) {

        return (HttpServletRequest request,
                HttpServletResponse response,
                AuthenticationException authException) ->
                writeUnauthorizedResponse(
                        response,
                        objectMapper,
                        authException.getMessage()
                );
    }

    private void writeUnauthorizedResponse(
            HttpServletResponse response,
            ObjectMapper objectMapper,
            String detail) throws IOException {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "UNAUTHORIZED");
        body.put("message",
                "Access denied. Only authorized users can upload documents.");
        body.put("details", List.of(detail));

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
