package com.rentivo.rental_saas.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    private static final String ACCESS_COOKIE = "ACCESS_TOKEN";
    private static final String REFRESH_COOKIE = "REFRESH_TOKEN";
    @Value("${security.cookie.secure:true}")
    private boolean secure;
    @Value("${security.cookie.same-site:Lax}")
    private String sameSite;
    @Value("${security.jwt.access-token-expiration}")
    private long accessTokenExpiration;
    @Value("${security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public void addAuthenticationCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    ) {
        ResponseCookie accessCookie = ResponseCookie
                .from(ACCESS_COOKIE, accessToken)
                .httpOnly(true)
                .secure(secure)
                .path("/api")
                .sameSite(sameSite)
                .maxAge(Duration.ofMillis(accessTokenExpiration))
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .path("/api/v1/auth")
                .sameSite(sameSite)
                .maxAge(Duration.ofMillis(refreshTokenExpiration))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );
    }

    public void clearAuthenticationCookies(
            HttpServletResponse response
    ) {
        ResponseCookie accessCookie = ResponseCookie
                .from(ACCESS_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .path("/api")
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .path("/api/v1/auth")
                .sameSite(sameSite)
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );
    }
}