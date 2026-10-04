package com.example.forgeHub.controller;

import com.example.forgeHub.dto.LoginRequest;
import com.example.forgeHub.dto.LoginResponse;
import com.example.forgeHub.dto.TokenResponse;
import com.example.forgeHub.dto.TwoFactorVerifyRequest;
import com.example.forgeHub.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        log.info("Login API called for user: {}", request.getEmail());

        LoginResponse loginResponse = authService.login(
                request.getEmail(),
                request.getPassword(),
                response
        );

        log.info("Login API processing completed for user: {}",
                request.getEmail());

        return ResponseEntity.ok(loginResponse);
    }

    // =========================================================
    // GOOGLE AUTHENTICATOR 2FA VERIFY
    //
    // Supports:
    // 1. First-time login -> 2FA_SETUP
    // 2. Normal login     -> 2FA_LOGIN
    // 3. Lost OTP recovery -> 2FA_RECOVERY
    // =========================================================

    @PostMapping("/2fa/verify")
    public ResponseEntity<TokenResponse> verify2FA(
            @Valid @RequestBody TwoFactorVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        log.info("2FA verification API called");

        String token = null;

        // ---------------------------------------------------------
        // 1. First check 2FA SETUP token
        // ---------------------------------------------------------

        token = getCookie(httpRequest, "twoFactorSetupToken");

        // ---------------------------------------------------------
        // 2. If not found, check RECOVERY token
        // ---------------------------------------------------------

        if (token == null || token.isBlank()) {

            token = getCookie(
                    httpRequest,
                    "twoFactorRecoveryToken"
            );
        }

        // ---------------------------------------------------------
        // 3. If not found, check NORMAL LOGIN token
        // ---------------------------------------------------------

        if (token == null || token.isBlank()) {

            token = getCookie(
                    httpRequest,
                    "twoFactorLoginToken"
            );
        }

        // ---------------------------------------------------------
        // 4. No temporary 2FA token found
        // ---------------------------------------------------------

        if (token == null || token.isBlank()) {

            log.warn("No 2FA temporary token found in request cookies");

            throw new RuntimeException(
                    "2FA session is missing or expired"
            );
        }

        log.debug("2FA temporary token found. Passing request to AuthService");

        // ---------------------------------------------------------
        // 5. Verify Google Authenticator OTP
        // ---------------------------------------------------------

        TokenResponse tokenResponse = authService.verify2FA(
                token,
                request.getCode(),
                response
        );

        log.info("2FA verification API completed successfully");

        // ---------------------------------------------------------
        // 6. Return ACCESS TOKEN
        // ---------------------------------------------------------

        return ResponseEntity.ok(tokenResponse);
    }

    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("Refresh token API called");

        String refreshToken = getRefreshTokenFromCookie(request);

        if (refreshToken == null || refreshToken.isBlank()) {

            log.warn("Refresh token cookie not found");

            throw new RuntimeException(
                    "Refresh token is missing"
            );
        }

        TokenResponse tokenResponse = authService.refresh(
                refreshToken,
                response
        );

        log.info("Refresh token API completed successfully");

        return ResponseEntity.ok(tokenResponse);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        log.info("Logout API called");

        String refreshToken = getRefreshTokenFromCookie(request);

        authService.logout(
                refreshToken,
                response
        );

        log.info("Logout completed successfully");

        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }

    // =========================================================
    // LOST OTP - SEND EMAIL OTP
    // =========================================================

    @PostMapping("/2fa/recovery/send")
    public ResponseEntity<String> sendRecoveryOtp(
            @RequestParam String email,
            HttpServletRequest request) {

        log.info(
                "2FA recovery OTP request received for email: {}",
                email
        );

        // ---------------------------------------------------------
        // Get current normal-login temporary token
        // ---------------------------------------------------------

        String loginToken = getCookie(
                request,
                "twoFactorLoginToken"
        );

        if (loginToken == null || loginToken.isBlank()) {

            log.warn(
                    "2FA login token missing for recovery OTP request"
            );

            throw new RuntimeException(
                    "2FA login session is missing or expired"
            );
        }

        // ---------------------------------------------------------
        // Send email OTP
        // ---------------------------------------------------------

        authService.sendRecoveryOtp(
                email,
                loginToken
        );

        log.info(
                "Recovery OTP sent successfully for email: {}",
                email
        );

        return ResponseEntity.ok(
                "OTP sent successfully to your email"
        );
    }

    // =========================================================
    // LOST OTP - VERIFY EMAIL OTP
    //
    // Email OTP successful:
    // 1. Generate NEW Google Authenticator secret
    // 2. Generate NEW QR
    // 3. Generate 2FA_RECOVERY temporary token
    // 4. Store token in HttpOnly cookie
    // 5. Return QR URL to frontend
    // =========================================================

    @PostMapping("/2fa/recovery/verify-email")
    public ResponseEntity<LoginResponse> verifyRecoveryOtp(
            @RequestParam String email,
            @Valid @RequestBody TwoFactorVerifyRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {

        log.info(
                "Recovery email OTP verification request received for email: {}",
                email
        );

        // ---------------------------------------------------------
        // Get normal login temporary token
        // ---------------------------------------------------------

        String loginToken = getCookie(
                httpRequest,
                "twoFactorLoginToken"
        );

        if (loginToken == null || loginToken.isBlank()) {

            log.warn(
                    "2FA login token missing for email OTP verification"
            );

            throw new RuntimeException(
                    "2FA login session is missing or expired"
            );
        }

        // ---------------------------------------------------------
        // Verify email OTP
        // ---------------------------------------------------------

        LoginResponse loginResponse = authService.verifyRecoveryOtp(
                email,
                request.getCode(),
                loginToken,
                response
        );

        log.info(
                "Recovery email OTP verified successfully for email: {}",
                email
        );

        // ---------------------------------------------------------
        // Important:
        // loginResponse contains:
        //
        // success = true
        // message = scan new QR and enter OTP
        // qrUrl   = NEW Google Authenticator QR URL
        // ---------------------------------------------------------

        return ResponseEntity.ok(loginResponse);
    }

    // =========================================================
    // GET REFRESH TOKEN FROM COOKIE
    // =========================================================

    private String getRefreshTokenFromCookie(
            HttpServletRequest request) {

        String refreshToken = getCookie(
                request,
                "refreshToken"
        );

        if (refreshToken == null || refreshToken.isBlank()) {

            log.warn("Refresh token cookie not found");
        }

        return refreshToken;
    }

    // =========================================================
    // GENERIC COOKIE HELPER
    // =========================================================

    private String getCookie(
            HttpServletRequest request,
            String cookieName) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if (cookieName.equals(cookie.getName())) {

                return cookie.getValue();
            }
        }

        return null;
    }
}