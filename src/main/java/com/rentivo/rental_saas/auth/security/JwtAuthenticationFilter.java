package com.rentivo.rental_saas.auth.security;

import com.rentivo.rental_saas.auth.entity.Users;
import com.rentivo.rental_saas.auth.repository.UserRepo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepo userRepository;

    private static @NonNull UsernamePasswordAuthenticationToken getUsernamePasswordAuthenticationToken(Users user) {
        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ACCESS"),
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().getRoleName()
                )
        );

        return new UsernamePasswordAuthenticationToken(
                user,
                null,
                authorities
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String uri = request.getServletPath();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                uri.equals("/api/v1/auth/login") ||
                uri.equals("/api/v1/auth/register") ||
                uri.equals("/api/v1/auth/forgot-password") ||
                uri.equals("/api/v1/auth/logout")) {

            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = null;

        // Refresh endpoint must exclusively use the refresh cookie.
        if (uri.equals("/api/v1/auth/refresh")) {

            token = extractCookie(request, "REFRESH_TOKEN");

        } else {

            // OTP and password-reset endpoints use Authorization headers.
            boolean tokenHeaderEndpoint =
                    uri.equals("/api/v1/auth/verify-otp") ||
                            uri.equals("/api/v1/auth/resend-otp") ||
                            uri.equals("/api/v1/auth/reset-password");

            if (tokenHeaderEndpoint) {

                String authHeader = request.getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }

            } else {

                // Normal protected endpoints use the access cookie.
                token = extractCookie(request, "ACCESS_TOKEN");
            }
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            if (!jwtService.isTokenValid(token)) {
                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid or expired token"
                );
                return;
            }

            String tokenType = jwtService.extractTokenType(token);

            if (List.of(
                    "REGISTER_OTP",
                    "LOGIN_OTP",
                    "PASSWORD_RESET_OTP"
            ).contains(tokenType)) {

                Long userId = jwtService.extractOtpUserId(token);

                if (!userRepository.existsById(userId)) {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "User not found"
                    );
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(tokenType))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else if ("ACCESS".equals(tokenType)) {

                String email = jwtService.extractEmail(token);

                Users user = userRepository.findByEmailWithRole(email)
                        .orElse(null);

                if (user == null || !user.isEnabled() || !user.isVerified()) {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "User account is unavailable"
                    );
                    return;
                }

                SecurityContextHolder.getContext()
                        .setAuthentication(getUsernamePasswordAuthenticationToken(user));

            } else if ("REFRESH".equals(tokenType)) {

                if (!uri.equals("/api/v1/auth/refresh")) {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "Refresh token cannot access this resource"
                    );
                    return;
                }

                String email = jwtService.extractEmail(token);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(new SimpleGrantedAuthority("REFRESH"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else if ("PASSWORD_RESET".equals(tokenType)) {

                Long userId = jwtService.extractPasswordResetUserId(token);

                if (!userRepository.existsById(userId)) {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED,
                            "User not found"
                    );
                    return;
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority("PASSWORD_RESET"))
                        );

                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {

                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid token type"
                );
                return;
            }

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractCookie(
            HttpServletRequest request,
            String cookieName
    ) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}