package ch.ofte.symphony.jafts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain configure(HttpSecurity http) throws Exception {

        return http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                )
//                .sessionManagement(session -> session
//                                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
//                        // whether A user can log in from multiple devices/browsers at the same time
////                        .maximumSessions(1)
////                        .maxSessionsPreventsLogin(true)
////                        .expiredUrl("/login?expired")
//                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/static/**",
                                "/images/**",
                                "/favicon.ico",
                                "/webjars/**",
                                "/api/**",
                                "/app/browser/**"
                        ).permitAll()
                        .requestMatchers("/public/**", "/").permitAll()
                        .anyRequest().authenticated()
                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/dashboard", true)
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/login?logout")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID", "remember-me")
//                        .addLogoutHandler(customLogoutHandler)
//                        .permitAll()
//                )
//                .userDetailsService(userDetailsService)
                .build();
    }
}
