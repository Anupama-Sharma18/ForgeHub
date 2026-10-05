package com.example.forgeHub.security;

import com.example.forgeHub.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(
            JwtService jwtService
    ) {
        this.jwtService = jwtService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri =
                request.getRequestURI();

        String method =
                request.getMethod();


        log.debug(
                "JWT authentication filter started. method={}, uri={}",
                method,
                requestUri
        );


        // =====================================================
        // 1. FIRST TRY TO GET ACCESS TOKEN FROM COOKIE
        // =====================================================

        String accessToken =
                getAccessTokenFromCookie(request);


        // =====================================================
        // 2. OPTIONAL FALLBACK - AUTHORIZATION HEADER
        // =====================================================
        //
        // Existing API testing / Postman ke liye useful hai.
        // Browser flow mein HttpOnly cookie use hogi.
        // =====================================================

        if (accessToken == null || accessToken.isBlank()) {

            String authHeader =
                    request.getHeader("Authorization");

            if (authHeader != null &&
                    authHeader.startsWith("Bearer ")) {

                accessToken =
                        authHeader.substring(7);
            }
        }


        // =====================================================
        // 3. NO ACCESS TOKEN
        // =====================================================

        if (accessToken == null ||
                accessToken.isBlank()) {

            log.debug(
                    "No access token found. Continuing request. uri={}",
                    requestUri
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =====================================================
        // 4. VERIFY JWT
        // =====================================================

        try {

            Claims claims =
                    jwtService.extractClaims(
                            accessToken
                    );


            log.debug(
                    "JWT signature and claims validated successfully. uri={}",
                    requestUri
            );


            // =================================================
            // 5. CHECK TOKEN TYPE
            // =================================================

            String tokenType =
                    claims.get(
                            "type",
                            String.class
                    );


            if (!"ACCESS".equals(tokenType)) {

                log.warn(
                        "Invalid JWT token type. expected=ACCESS, actual={}, uri={}",
                        tokenType,
                        requestUri
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 6. GET EMAIL
            // =================================================

            String email =
                    claims.getSubject();


            if (email == null ||
                    email.isBlank()) {

                log.warn(
                        "JWT subject/email is missing. uri={}",
                        requestUri
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 7. GET ROLE
            // =================================================

            String role =
                    claims.get(
                            "role",
                            String.class
                    );


            if (role == null ||
                    role.isBlank()) {

                log.warn(
                        "JWT role is missing for user: {}",
                        email
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // 8. CREATE AUTHENTICATION
            // =================================================

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(

                            email,

                            null,

                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            )
                    );


            // =================================================
            // 9. SET SECURITY CONTEXT
            // =================================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );


            log.debug(
                    "JWT authentication successful. user={}, role={}, uri={}",
                    email,
                    role,
                    requestUri
            );


        } catch (JwtException e) {

            log.warn(
                    "JWT validation failed. uri={}, reason={}",
                    requestUri,
                    e.getMessage()
            );


        } catch (IllegalArgumentException e) {

            log.warn(
                    "Invalid JWT argument received. uri={}, reason={}",
                    requestUri,
                    e.getMessage()
            );
        }


        // =====================================================
        // 10. CONTINUE FILTER CHAIN
        // =====================================================

        filterChain.doFilter(
                request,
                response
        );


        log.debug(
                "JWT authentication filter completed. method={}, uri={}, status={}",
                method,
                requestUri,
                response.getStatus()
        );
    }


    // =========================================================
    // GET ACCESS TOKEN FROM HTTPONLY COOKIE
    // =========================================================

    private String getAccessTokenFromCookie(
            HttpServletRequest request
    ) {

        Cookie[] cookies =
                request.getCookies();


        if (cookies == null) {
            return null;
        }


        for (Cookie cookie : cookies) {

            if ("accessToken".equals(
                    cookie.getName()
            )) {

                return cookie.getValue();
            }
        }


        return null;
    }
}
