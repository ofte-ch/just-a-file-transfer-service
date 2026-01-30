package ch.ofte.symphony.jafts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) {
        try {
            return http
                    .cors(Customizer.withDefaults())
                    .csrf(csrf -> csrf
                            // Enable CSRF with cookie-based token (readable by JavaScript)
                            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                            // CSRF protection for all endpoints including /api/**
                    )
                    .sessionManagement(session -> session
                            // Always create session for CSRF + Session ID protection
                            .sessionFixation().newSession()
                    )
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(
                                    "/css/**",
                                    "/js/**",
                                    "/static/**",
                                    "/images/**",
                                    "/favicon.ico",
                                    "/webjars/**",
                                    "/api/**"
                            ).permitAll()
                            .requestMatchers("/public/**", "/").permitAll()
                            .anyRequest().authenticated()
                    )
                    .build();
        } catch (Exception e) {
            throw new SecurityConfigurationException("Failed to configure Spring Security filter chain", e);
        }
    }

    /**
     * Custom exception for security configuration errors
     */
    public static class SecurityConfigurationException extends RuntimeException {
        public SecurityConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
