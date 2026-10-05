package com.example.forgeHub.config;

import com.example.forgeHub.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    // =========================================================
    // PASSWORD ENCODER
    // =========================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {

        this.jwtAuthenticationFilter =
                jwtAuthenticationFilter;
    }


    // =========================================================
    // SECURITY FILTER CHAIN
    // =========================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

                // -------------------------------------------------
                // CSRF
                // -------------------------------------------------

                .csrf(csrf -> csrf.disable())


                // -------------------------------------------------
                // STATELESS
                // -------------------------------------------------

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                // -------------------------------------------------
                // AUTHORIZATION
                // -------------------------------------------------

                .authorizeHttpRequests(auth -> auth


                        // =========================================
                        // PUBLIC THYMELEAF PAGES
                        // =========================================

                        .requestMatchers(
                                "/",
                                "/login",
                                "/2fa",
                                "/2fa/setup",
                                "/2fa/recovery/**",
                                "/forgot-password"
                        ).permitAll()


                        // =========================================
                        // STATIC FILES
                        // =========================================

                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico"
                        ).permitAll()


                        // =========================================
                        // AUTH APIs
                        // =========================================

                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/logout",
                                "/auth/2fa/**"
                        ).permitAll()


                        // =========================================
                        // DASHBOARD HTML SHELL
                        // =========================================
                        //
                        // IMPORTANT:
                        // Access token sessionStorage me hai.
                        // Direct browser navigation me Bearer
                        // header automatically nahi jaata.
                        //
                        // Isliye page shell public rahega.
                        // Actual ADMIN/VENDOR APIs protected rahengi.
                        // =========================================

                        .requestMatchers(
                                "/admin/dashboard",
                                "/vendor/dashboard"
                        ).permitAll()


                        // =========================================
                        // ADMIN APIs
                        // =========================================

                        .requestMatchers(
                                "/api/admin/**"
                        ).hasRole("ADMIN")


                        // =========================================
                        // VENDOR APIs
                        // =========================================

                        .requestMatchers(
                                "/api/vendor/**"
                        ).hasRole("VENDOR")


                        // =========================================
                        // COMMON AUTHENTICATED APIs
                        // =========================================

                        .requestMatchers(
                                "/api/test/**"
                        ).authenticated()


                        // =========================================
                        // EVERYTHING ELSE
                        // =========================================

                        .anyRequest().authenticated()
                )


                // -------------------------------------------------
                // JWT FILTER
                // -------------------------------------------------

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }
}