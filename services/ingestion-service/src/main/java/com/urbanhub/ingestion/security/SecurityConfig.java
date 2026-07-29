package com.urbanhub.ingestion.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;
    private final ApiKeyAuthenticationEntryPoint authenticationEntryPoint;
    private final RateLimitFilter rateLimitFilter;


    public SecurityConfig(
            ApiKeyAuthenticationFilter apiKeyAuthenticationFilter,
            RateLimitFilter rateLimitFilter,
            ApiKeyAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.apiKeyAuthenticationFilter =
                apiKeyAuthenticationFilter;
        this.rateLimitFilter =
                rateLimitFilter;
        this.authenticationEntryPoint =
                authenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                )
                .authorizeHttpRequests(authentication ->
                        authentication
                                .requestMatchers(
                                        "/actuator/health/**",
                                        "/actuator/info",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/v3/api-docs/**"
                                )
                                .permitAll()
                                .requestMatchers(
                                        "/api/ingestion/measurements"
                                )
                                .hasRole("SENSOR")
                                .anyRequest()
                                .denyAll()
                )
                .addFilterBefore(
                        apiKeyAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(
                        rateLimitFilter,
                        ApiKeyAuthenticationFilter.class
                )
                .build();
    }
}
