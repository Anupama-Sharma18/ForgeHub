package com.example.forgeHub.security;

import com.example.forgeHub.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String method = request.getMethod();

        log.debug(
                "JWT authentication filter started. method={}, uri={}",
                method,
                requestUri
        );

        // ==========================================
        // 1. Read Authorization header
        // ==========================================

        String authHeader =
                request.getHeader("Authorization");

        // ==========================================
        // 2. Token nahi hai
        // ==========================================

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            log.debug(
                    "No Bearer access token found. Continuing request. uri={}",
                    requestUri
            );

            filterChain.doFilter(request, response);
            return;
        }

        // ==========================================
        // 3. Extract Access Token
        // ==========================================

        String accessToken =
                authHeader.substring(7);

        // IMPORTANT:
        // Access token ko kabhi log nahi karna.

        try {

            // ==========================================
            // 4. Verify JWT
            // ==========================================

            Claims claims =
                    jwtService.extractClaims(accessToken);

            log.debug(
                    "JWT signature and claims validated successfully. uri={}",
                    requestUri
            );

            // ==========================================
            // 5. Check token type
            // ==========================================

            String tokenType =
                    claims.get("type", String.class);

            if (!"ACCESS".equals(tokenType)) {

                log.warn(
                        "Invalid JWT token type received. expected=ACCESS, actual={}, uri={}",
                        tokenType,
                        requestUri
                );

                filterChain.doFilter(request, response);
                return;
            }

            // ==========================================
            // 6. Get username/email
            // ==========================================

            String email =
                    claims.getSubject();

            if (email == null || email.isBlank()) {

                log.warn(
                        "JWT subject/email is missing. uri={}",
                        requestUri
                );

                filterChain.doFilter(request, response);
                return;
            }

            // ==========================================
            // 7. Get role
            // ==========================================

            String role =
                    claims.get("role", String.class);

            if (role == null || role.isBlank()) {

                log.warn(
                        "JWT role is missing for user: {}",
                        email
                );

                filterChain.doFilter(request, response);
                return;
            }

            // ==========================================
            // 8. Create Authentication object
            // ==========================================

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

            // ==========================================
            // 9. Put authentication into SecurityContext
            // ==========================================

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            log.debug(
                    "JWT authentication successful. user={}, role={}, uri={}",
                    email,
                    role,
                    requestUri
            );

        } catch (JwtException e) {

            // ==========================================
            // Invalid / expired JWT
            // ==========================================

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

        // ==========================================
        // 10. Continue filter chain
        // ==========================================

        filterChain.doFilter(request, response);

        log.debug(
                "JWT authentication filter completed. method={}, uri={}, status={}",
                method,
                requestUri,
                response.getStatus()
        );
    }
}