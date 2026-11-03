package com.rentivo.rental_saas.auth.config;

import com.rentivo.rental_saas.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/register",
                                "/api/v1/auth/login",
                                "/api/v1/auth/forgot-password"
                        ).permitAll()

                        // Refresh token only
                        .requestMatchers("/api/v1/auth/refresh")
                        .hasAuthority("REFRESH")

                        // OTP token only
                        .requestMatchers(
                                "/api/v1/auth/verify-otp",
                                "/api/v1/auth/resend-otp"
                        ).hasAnyAuthority(
                                "REGISTER_OTP",
                                "LOGIN_OTP",
                                "PASSWORD_RESET_OTP"
                        )

                        // Password reset token only
                        .requestMatchers("/api/v1/auth/reset-password")
                        .hasAuthority("PASSWORD_RESET")

                        // Change password requires access token
                        .requestMatchers("/api/v1/auth/change-password")
                        .hasAuthority("ACCESS")

                        // Role-based access
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("SUPER_ADMIN")

                        .requestMatchers("/api/v1/agency/**")
                        .hasAnyRole(
                                "SUPER_ADMIN",
                                "AGENCY_ADMIN",
                                "AGENCY_AGENT",
                                "AGENCY_ACCOUNTANT"
                        )

                        .requestMatchers("/api/v1/landlord/**")
                        .hasAnyRole("SUPER_ADMIN", "LANDLORD")

                        .requestMatchers("/api/v1/tenant/**")
                        .hasAnyRole("SUPER_ADMIN", "TENANT")

                        // Remaining endpoints require ACCESS
                        .anyRequest().hasAuthority("ACCESS")
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}