package com.example.forgeHub.serviceImpl;

import com.example.forgeHub.dto.LoginResponse;
import com.example.forgeHub.dto.TokenResponse;
import com.example.forgeHub.exception.InvalidCredentialsException;
import com.example.forgeHub.model.User;
import com.example.forgeHub.repository.UserRepository;
import com.example.forgeHub.service.AuthService;
import com.example.forgeHub.service.EmailService;
import com.example.forgeHub.service.GoogleAuthService;
import com.example.forgeHub.service.JwtService;
import com.example.forgeHub.util.TokenHashUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenHashUtil tokenHashUtil;
    private final GoogleAuthService googleAuthService;
    private final EmailService emailService;


    // =========================================================
    // LOGIN
    // =========================================================

    @Override
    @Transactional
    public LoginResponse login(
            String email,
            String password,
            HttpServletResponse response
    ) {

        log.info(
                "Login attempt received for user: {}",
                email
        );


        // -----------------------------------------------------
        // STEP 1: Find user
        // -----------------------------------------------------

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Login failed - user not found: {}",
                                    email
                            );

                            return new InvalidCredentialsException(
                                    "Invalid email or password"
                            );
                        });


        // -----------------------------------------------------
        // STEP 2: Verify password
        // -----------------------------------------------------

        if (!passwordEncoder.matches(
                password,
                user.getPasswordHash()
        )) {

            log.warn(
                    "Login failed - invalid password for user: {}",
                    email
            );

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }


        log.info(
                "Password verified successfully for user: {}",
                email
        );


        // =====================================================
        // FIRST TIME LOGIN
        // =====================================================

        if (Boolean.TRUE.equals(
                user.getIsFirstTimeLogin()
        )) {

            log.info(
                    "First-time login detected for user: {}",
                    email
            );


            // -------------------------------------------------
            // STEP 3: Generate secret key
            // -------------------------------------------------

            if (user.getSecretKey() == null ||
                    user.getSecretKey().isBlank()) {

                String secret =
                        googleAuthService.generateSecret();

                user.setSecretKey(secret);

                userRepository.save(user);

                log.info(
                        "New 2FA secret generated for user: {}",
                        email
                );
            }


            // -------------------------------------------------
            // STEP 4: Generate QR URL
            // -------------------------------------------------

            String qrUrl =
                    googleAuthService.generateQrUrl(
                            user.getEmail(),
                            user.getSecretKey()
                    );


            log.info(
                    "2FA QR URL generated for first-time user: {}",
                    email
            );


            // -------------------------------------------------
            // STEP 5: Generate setup token
            // -------------------------------------------------

            String setupToken =
                    jwtService.generateTwoFactorSetupToken(
                            user
                    );


            // -------------------------------------------------
            // STEP 6: Store setup token in cookie
            // -------------------------------------------------

            addTwoFactorSetupCookie(
                    response,
                    setupToken
            );


            log.info(
                    "2FA setup challenge created for user: {}",
                    email
            );


            // -------------------------------------------------
            // No access token
            // No refresh token
            //
            // OTP verification ke baad hi token milega.
            // -------------------------------------------------

            return new LoginResponse(
                    true,
                    "Scan the QR code using Google Authenticator and enter the 6-digit code",
                    qrUrl
            );
        }


        // =====================================================
        // NORMAL / SECOND LOGIN
        // =====================================================

        log.info(
                "Normal login detected for existing user: {}",
                email
        );


        // -----------------------------------------------------
        // STEP 3: Check existing 2FA secret
        // -----------------------------------------------------

        if (user.getSecretKey() == null ||
                user.getSecretKey().isBlank()) {

            log.error(
                    "2FA secret key missing for existing user: {}",
                    email
            );

            throw new RuntimeException(
                    "2FA is not configured for this user"
            );
        }


        // -----------------------------------------------------
        // STEP 4: Generate 2FA login token
        // -----------------------------------------------------

        String loginToken =
                jwtService.generateTwoFactorLoginToken(
                        user
                );


        // -----------------------------------------------------
        // STEP 5: Store login token in HttpOnly cookie
        // -----------------------------------------------------

        addTwoFactorLoginCookie(
                response,
                loginToken
        );


        log.info(
                "2FA login challenge created for user: {}",
                email
        );


        // -----------------------------------------------------
        // No access token yet
        // User must verify Google Authenticator OTP
        // -----------------------------------------------------

        return new LoginResponse(
                false,
                "Enter the 6-digit Google Authenticator code. If you lost your OTP, click 'Lost your OTP?'.",
                null
        );
    }


    // =========================================================
    // GOOGLE AUTHENTICATOR 2FA
    //
    // Supports:
    // 1. 2FA_SETUP
    // 2. 2FA_LOGIN
    // 3. 2FA_RECOVERY
    // =========================================================

    @Override
    @Transactional
    public TokenResponse verify2FA(
            String token,
            String code,
            HttpServletResponse response
    ) {

        log.info(
                "2FA verification request received"
        );


        // -----------------------------------------------------
        // STEP 1: Check token
        // -----------------------------------------------------

        if (token == null ||
                token.isBlank()) {

            log.warn(
                    "2FA token missing"
            );

            throw new RuntimeException(
                    "2FA token is missing or expired"
            );
        }


        // -----------------------------------------------------
        // STEP 2: Validate OTP format
        // -----------------------------------------------------

        if (code == null ||
                !code.matches("\\d{6}")) {

            log.warn(
                    "Invalid 2FA OTP format"
            );

            throw new RuntimeException(
                    "OTP must be exactly 6 digits"
            );
        }


        try {

            // -------------------------------------------------
            // STEP 3: Extract token type
            // -------------------------------------------------

            String tokenType =
                    jwtService.extractTokenType(
                            token
                    );


            log.info(
                    "2FA token type identified as: {}",
                    tokenType
            );


            // -------------------------------------------------
            // STEP 4: Only allowed temporary tokens
            // -------------------------------------------------

            if (!"2FA_SETUP".equals(tokenType)
                    && !"2FA_LOGIN".equals(tokenType)
                    && !"2FA_RECOVERY".equals(tokenType)) {

                log.warn(
                        "Invalid 2FA token type: {}",
                        tokenType
                );

                throw new RuntimeException(
                        "Invalid 2FA token"
                );
            }


            // -------------------------------------------------
            // STEP 5: Extract email
            // -------------------------------------------------

            String email =
                    jwtService.extractUsername(
                            token
                    );


            log.info(
                    "2FA verification started for user: {}",
                    email
            );


            // -------------------------------------------------
            // STEP 6: Find user
            // -------------------------------------------------

            User user =
                    userRepository
                            .findByEmail(email)
                            .orElseThrow(() -> {

                                log.warn(
                                        "User not found during 2FA verification: {}",
                                        email
                                );

                                return new RuntimeException(
                                        "User not found"
                                );
                            });


            // -------------------------------------------------
            // STEP 7: Check secret key
            // -------------------------------------------------

            if (user.getSecretKey() == null ||
                    user.getSecretKey().isBlank()) {

                log.error(
                        "2FA secret key not found for user: {}",
                        email
                );

                throw new RuntimeException(
                        "2FA secret key not found"
                );
            }


            // -------------------------------------------------
            // STEP 8: Convert OTP
            // -------------------------------------------------

            int otp;

            try {

                otp = Integer.parseInt(code);

            } catch (NumberFormatException e) {

                log.warn(
                        "Unable to parse OTP for user: {}",
                        email
                );

                throw new RuntimeException(
                        "Invalid OTP"
                );
            }


            // -------------------------------------------------
            // STEP 9: Verify Google Authenticator OTP
            // -------------------------------------------------

            boolean valid =
                    googleAuthService.verifyCode(
                            user.getSecretKey(),
                            otp
                    );


            if (!valid) {

                log.warn(
                        "Invalid Google Authenticator OTP for user: {}",
                        email
                );

                throw new RuntimeException(
                        "Invalid 2FA code"
                );
            }


            log.info(
                    "Google Authenticator OTP verified successfully for user: {}",
                    email
            );


            // =================================================
            // FIRST LOGIN SPECIFIC LOGIC
            // =================================================

            if ("2FA_SETUP".equals(tokenType)) {

                log.info(
                        "Completing first-time 2FA setup for user: {}",
                        email
                );


                user.setIsFirstTimeLogin(false);

                userRepository.save(user);

                clearTwoFactorSetupCookie(
                        response
                );


                log.info(
                        "First-time 2FA setup completed for user: {}",
                        email
                );
            }


            // =================================================
            // NORMAL LOGIN SPECIFIC LOGIC
            // =================================================

            if ("2FA_LOGIN".equals(tokenType)) {

                log.info(
                        "Completing normal login after 2FA for user: {}",
                        email
                );

                clearTwoFactorLoginCookie(
                        response
                );
            }


            // =================================================
            // RECOVERY LOGIN SPECIFIC LOGIC
            // =================================================

            if ("2FA_RECOVERY".equals(tokenType)) {

                log.info(
                        "Completing login after 2FA recovery for user: {}",
                        email
                );

                clearTwoFactorRecoveryCookie(
                        response
                );
            }


            // =================================================
            // ISSUE ACCESS + REFRESH TOKEN
            // =================================================

            // -------------------------------------------------
            // STEP 10: Generate access token
            // -------------------------------------------------

            String accessToken =
                    jwtService.generateAccessToken(
                            user
                    );


            log.info(
                    "Access token generated for user: {}",
                    email
            );


            // -------------------------------------------------
            // STEP 11: Generate refresh JTI
            // -------------------------------------------------

            String refreshJti =
                    UUID.randomUUID().toString();


            // -------------------------------------------------
            // STEP 12: Generate refresh token
            // -------------------------------------------------

            String refreshToken =
                    jwtService.generateRefreshToken(
                            user,
                            refreshJti
                    );


            // -------------------------------------------------
            // STEP 13: Hash refresh token
            // -------------------------------------------------

            String refreshTokenHash =
                    tokenHashUtil.hash(
                            refreshToken
                    );


            // -------------------------------------------------
            // STEP 14: Save refresh information
            // -------------------------------------------------

            user.setRefreshJti(
                    refreshJti
            );

            user.setRefreshTokenHash(
                    refreshTokenHash
            );

            user.setRevoked(false);

            userRepository.save(user);


            log.info(
                    "Refresh token information saved for user: {}",
                    email
            );


            // -------------------------------------------------
            // STEP 15: Refresh token cookie
            // -------------------------------------------------

            addRefreshCookie(
                    response,
                    refreshToken
            );


            log.info(
                    "Login successful after 2FA for user: {}",
                    email
            );


            // -------------------------------------------------
            // STEP 16: Return access token
            // -------------------------------------------------

            return new TokenResponse(
                    accessToken
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Unexpected error during 2FA verification",
                    e
            );

            throw new RuntimeException(
                    "2FA verification failed",
                    e
            );
        }
    }


    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Override
    @Transactional
    public TokenResponse refresh(
            String refreshToken,
            HttpServletResponse response
    ) {

        log.info(
                "Refresh token request received"
        );


        if (refreshToken == null ||
                refreshToken.isBlank()) {

            log.warn(
                    "Refresh token missing"
            );

            throw new RuntimeException(
                    "Refresh token missing"
            );
        }


        try {

            // -------------------------------------------------
            // STEP 1: Check token type
            // -------------------------------------------------

            String tokenType =
                    jwtService.extractTokenType(
                            refreshToken
                    );


            if (!"REFRESH".equals(tokenType)) {

                log.warn(
                        "Invalid refresh token type"
                );

                throw new RuntimeException(
                        "Invalid token type"
                );
            }


            // -------------------------------------------------
            // STEP 2: Extract JTI
            // -------------------------------------------------

            String jti =
                    jwtService.extractJti(
                            refreshToken
                    );


            // -------------------------------------------------
            // STEP 3: Extract email
            // -------------------------------------------------

            String email =
                    jwtService.extractUsername(
                            refreshToken
                    );


            // -------------------------------------------------
            // STEP 4: Find user using JTI
            // -------------------------------------------------

            User user =
                    userRepository
                            .findByRefreshJti(jti)
                            .orElseThrow(() -> {

                                log.warn(
                                        "Refresh JTI not found"
                                );

                                return new RuntimeException(
                                        "Refresh token not found"
                                );
                            });


            // -------------------------------------------------
            // STEP 5: Check revoked
            // -------------------------------------------------

            if (Boolean.TRUE.equals(
                    user.getRevoked()
            )) {

                log.warn(
                        "Revoked refresh token used by user: {}",
                        email
                );

                throw new RuntimeException(
                        "Refresh token has been revoked"
                );
            }


            // -------------------------------------------------
            // STEP 6: Verify email
            // -------------------------------------------------

            if (!user.getEmail().equals(email)) {

                log.warn(
                        "Refresh token user mismatch"
                );

                throw new RuntimeException(
                        "Invalid refresh token"
                );
            }


            // -------------------------------------------------
            // STEP 7: Hash incoming refresh token
            // -------------------------------------------------

            String incomingHash =
                    tokenHashUtil.hash(
                            refreshToken
                    );


            // -------------------------------------------------
            // STEP 8: Compare hash
            // -------------------------------------------------

            if (user.getRefreshTokenHash() == null ||
                    !incomingHash.equals(
                            user.getRefreshTokenHash()
                    )) {

                log.warn(
                        "Refresh token hash mismatch for user: {}",
                        email
                );

                throw new RuntimeException(
                        "Invalid refresh token"
                );
            }


            // -------------------------------------------------
            // STEP 9: New access token
            // -------------------------------------------------

            String newAccessToken =
                    jwtService.generateAccessToken(
                            user
                    );


            // -------------------------------------------------
            // STEP 10: New JTI
            // -------------------------------------------------

            String newJti =
                    UUID.randomUUID().toString();


            // -------------------------------------------------
            // STEP 11: New refresh token
            // -------------------------------------------------

            String newRefreshToken =
                    jwtService.generateRefreshToken(
                            user,
                            newJti
                    );


            // -------------------------------------------------
            // STEP 12: Hash new refresh token
            // -------------------------------------------------

            String newRefreshHash =
                    tokenHashUtil.hash(
                            newRefreshToken
                    );


            // -------------------------------------------------
            // STEP 13: Update DB
            // -------------------------------------------------

            user.setRefreshJti(
                    newJti
            );

            user.setRefreshTokenHash(
                    newRefreshHash
            );

            user.setRevoked(false);

            userRepository.save(user);


            // -------------------------------------------------
            // STEP 14: Replace refresh cookie
            // -------------------------------------------------

            addRefreshCookie(
                    response,
                    newRefreshToken
            );


            log.info(
                    "Access token refreshed successfully for user: {}",
                    email
            );


            return new TokenResponse(
                    newAccessToken
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            log.error(
                    "Refresh token processing failed",
                    e
            );

            throw new RuntimeException(
                    "Invalid or expired refresh token",
                    e
            );
        }
    }


    // =========================================================
    // LOGOUT
    // =========================================================

    @Override
    @Transactional
    public void logout(
            String refreshToken,
            HttpServletResponse response
    ) {

        log.info(
                "Logout request received"
        );


        if (refreshToken != null &&
                !refreshToken.isBlank()) {

            try {

                // -------------------------------------------------
                // STEP 1: Extract JTI
                // -------------------------------------------------

                String jti =
                        jwtService.extractJti(
                                refreshToken
                        );


                // -------------------------------------------------
                // STEP 2: Find user and revoke token
                // -------------------------------------------------

                userRepository
                        .findByRefreshJti(jti)
                        .ifPresent(user -> {

                            user.setRevoked(true);

                            userRepository.save(user);

                            log.info(
                                    "Refresh token revoked for user: {}",
                                    user.getEmail()
                            );
                        });

            } catch (Exception e) {

                log.warn(
                        "Invalid or expired refresh token during logout"
                );
            }
        }


        // -----------------------------------------------------
        // STEP 3: Clear refresh cookie
        // -----------------------------------------------------

        clearRefreshCookie(
                response
        );


        log.info(
                "Logout completed"
        );
    }


    // =========================================================
    // SEND RECOVERY EMAIL OTP
    // =========================================================

    @Override
    public void sendRecoveryOtp(
            String email,
            String loginToken
    ) {

        log.info(
                "2FA recovery OTP request received for email: {}",
                email
        );


        // -----------------------------------------------------
        // STEP 1: Validate current 2FA login token
        // -----------------------------------------------------

        User user =
                getUserFromTwoFactorToken(
                        loginToken,
                        "2FA_LOGIN"
                );


        // -----------------------------------------------------
        // STEP 2: Verify email matches account
        // -----------------------------------------------------

        if (!user.getEmail()
                .equalsIgnoreCase(email)) {

            log.warn(
                    "Recovery email does not match login user: {}",
                    email
            );

            throw new RuntimeException(
                    "Email does not match the logged-in account"
            );
        }


        // -----------------------------------------------------
        // STEP 3: Send email OTP
        // -----------------------------------------------------

        emailService.sendOtp(
                user.getEmail()
        );


        log.info(
                "2FA recovery OTP sent successfully"
        );
    }


    // =========================================================
    // VERIFY RECOVERY EMAIL OTP
    // =========================================================

    @Override
    @Transactional
    public LoginResponse verifyRecoveryOtp(
            String email,
            String otp,
            String loginToken,
            HttpServletResponse response
    ) {

        log.info(
                "Recovery email OTP verification requested"
        );


        // -----------------------------------------------------
        // STEP 1: Validate 2FA login token
        // -----------------------------------------------------

        User user =
                getUserFromTwoFactorToken(
                        loginToken,
                        "2FA_LOGIN"
                );


        // -----------------------------------------------------
        // STEP 2: Verify email
        // -----------------------------------------------------

        if (!user.getEmail()
                .equalsIgnoreCase(email)) {

            log.warn(
                    "Recovery email mismatch"
            );

            throw new RuntimeException(
                    "Email does not match the logged-in account"
            );
        }


        // -----------------------------------------------------
        // STEP 3: Verify email OTP
        // -----------------------------------------------------

        boolean verified =
                emailService.verifyOtp(
                        user.getEmail(),
                        otp
                );


        if (!verified) {

            log.warn(
                    "Invalid or expired recovery email OTP"
            );

            throw new RuntimeException(
                    "Invalid or expired email OTP"
            );
        }


        log.info(
                "Recovery email OTP verified successfully"
        );


        // =====================================================
        // EMAIL OTP SUCCESS
        // =====================================================


        // -----------------------------------------------------
        // STEP 4: Generate NEW Google Authenticator secret
        // -----------------------------------------------------

        String newSecret =
                googleAuthService.generateSecret();


        // -----------------------------------------------------
        // STEP 5: Replace OLD secret
        // -----------------------------------------------------

        user.setSecretKey(
                newSecret
        );

        // Existing user hai, so first-time login
        // false hi rahega.
        user.setIsFirstTimeLogin(false);


        userRepository.save(user);


        log.info(
                "Old Google Authenticator secret replaced for user: {}",
                user.getEmail()
        );


        // -----------------------------------------------------
        // STEP 6: Generate NEW QR URL
        // -----------------------------------------------------

        String qrUrl =
                googleAuthService.generateQrUrl(
                        user.getEmail(),
                        newSecret
                );


        log.info(
                "New 2FA recovery QR generated for user: {}",
                user.getEmail()
        );


        // -----------------------------------------------------
        // STEP 7: Generate temporary recovery token
        // -----------------------------------------------------

        String recoveryToken =
                jwtService.generateTwoFactorRecoveryToken(
                        user
                );


        // -----------------------------------------------------
        // STEP 8: Store recovery token in HttpOnly cookie
        // -----------------------------------------------------

        addTwoFactorRecoveryCookie(
                response,
                recoveryToken
        );


        // -----------------------------------------------------
        // IMPORTANT:
        // Old 2FA_LOGIN cookie clear karna zaroori hai.
        //
        // Warna /2fa/verify mein old login token pick ho
        // sakta hai instead of recovery token.
        // -----------------------------------------------------

        clearTwoFactorLoginCookie(
                response
        );


        log.info(
                "2FA recovery challenge created for user: {}",
                user.getEmail()
        );


        // -----------------------------------------------------
        // STEP 9: Return NEW QR
        // -----------------------------------------------------

        return new LoginResponse(
                true,
                "Email verification successful. Scan the new QR code using Google Authenticator and enter the 6-digit code.",
                qrUrl
        );
    }


    // =========================================================
    // GET USER FROM TEMPORARY 2FA TOKEN
    // =========================================================

    private User getUserFromTwoFactorToken(
            String token,
            String expectedType
    ) {

        // -----------------------------------------------------
        // STEP 1: Token check
        // -----------------------------------------------------

        if (token == null ||
                token.isBlank()) {

            log.warn(
                    "Temporary 2FA token missing"
            );

            throw new RuntimeException(
                    "2FA token is missing or expired"
            );
        }


        // -----------------------------------------------------
        // STEP 2: Token type check
        // -----------------------------------------------------

        String tokenType =
                jwtService.extractTokenType(
                        token
                );


        if (!expectedType.equals(tokenType)) {

            log.warn(
                    "Unexpected token type. Expected: {}, Actual: {}",
                    expectedType,
                    tokenType
            );

            throw new RuntimeException(
                    "Invalid 2FA token"
            );
        }


        // -----------------------------------------------------
        // STEP 3: Extract email
        // -----------------------------------------------------

        String email =
                jwtService.extractUsername(
                        token
                );


        // -----------------------------------------------------
        // STEP 4: Find user
        // -----------------------------------------------------

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> {

                    log.warn(
                            "User not found from 2FA token"
                    );

                    return new RuntimeException(
                            "User not found"
                    );
                });
    }


    // =========================================================
    // 2FA SETUP COOKIE
    // =========================================================

    private void addTwoFactorSetupCookie(
            HttpServletResponse response,
            String setupToken
    ) {

        Cookie cookie =
                new Cookie(
                        "twoFactorSetupToken",
                        setupToken
                );

        cookie.setHttpOnly(true);

        // Local HTTP testing
        cookie.setSecure(false);

        cookie.setPath(
                "/auth/2fa"
        );

        // 10 minutes
        cookie.setMaxAge(
                10 * 60
        );

        response.addCookie(
                cookie
        );

        log.debug(
                "2FA setup cookie created"
        );
    }


    // =========================================================
    // CLEAR 2FA SETUP COOKIE
    // =========================================================

    private void clearTwoFactorSetupCookie(
            HttpServletResponse response
    ) {

        Cookie cookie =
                new Cookie(
                        "twoFactorSetupToken",
                        ""
                );

        cookie.setHttpOnly(true);

        cookie.setSecure(false);

        cookie.setPath(
                "/auth/2fa"
        );

        cookie.setMaxAge(0);

        response.addCookie(
                cookie
        );

        log.debug(
                "2FA setup cookie cleared"
        );
    }


    // =========================================================
    // 2FA LOGIN COOKIE
    // =========================================================

    private void addTwoFactorLoginCookie(
            HttpServletResponse response,
            String loginToken
    ) {

        Cookie cookie =
                new Cookie(
                        "twoFactorLoginToken",
                        loginToken
                );

        cookie.setHttpOnly(true);

        // Local HTTP testing
        cookie.setSecure(false);

        cookie.setPath(
                "/auth/2fa"
        );

        // 10 minutes
        cookie.setMaxAge(
                10 * 60
        );

        response.addCookie(
                cookie
        );

        log.debug(
                "2FA login cookie created"
        );
    }


    // =========================================================
    // CLEAR 2FA LOGIN COOKIE
    // =========================================================

    private void clearTwoFactorLoginCookie(
            HttpServletResponse response
    ) {

        Cookie cookie =
                new Cookie(
                        "twoFactorLoginToken",
                        ""
                );

        cookie.setHttpOnly(true);

        cookie.setSecure(false);

        cookie.setPath(
                "/auth/2fa"
        );

        cookie.setMaxAge(0);

        response.addCookie(
                cookie
        );

        log.debug(
                "2FA login cookie cleared"
        );
    }


    // =========================================================
    // 2FA RECOVERY COOKIE
    // =========================================================

    private void addTwoFactorRecoveryCookie(
            HttpServletResponse response,
            String recoveryToken
    ) {

        Cookie cookie =
                new Cookie(
                        "twoFactorRecoveryToken",
                        recoveryToken
                );

        cookie.setHttpOnly(true);

        // Local HTTP testing
        cookie.setSecure(false);

        cookie.setPath(
                "/auth/2fa"
        );

        // 10 minutes
        cookie.setMaxAge(
                10 * 60
        );

        response.addCookie(
                cookie
        );

        log.debug(
                "2FA recovery cookie created"
        );
    }


    // =========================================================
    // CLEAR 2FA RECOVERY COOKIE
    // =========================================================

    private void clearTwoFactorRecoveryCookie(
            HttpServletResponse response
    ) {

        Cookie cookie =
                new Cookie(
                        "twoFactorRecoveryToken",
                        ""
                );

        cookie.setHttpOnly(true);

        cookie.setSecure(false);

        cookie.setPath(
                "/auth/2fa"
        );

        cookie.setMaxAge(0);

        response.addCookie(
                cookie
        );

        log.debug(
                "2FA recovery cookie cleared"
        );
    }


    // =========================================================
    // REFRESH COOKIE
    // =========================================================

    private void addRefreshCookie(
            HttpServletResponse response,
            String refreshToken
    ) {

        Cookie cookie =
                new Cookie(
                        "refreshToken",
                        refreshToken
                );

        // JavaScript cannot access refresh token
        cookie.setHttpOnly(true);

        // Local HTTP testing
        cookie.setSecure(false);

        cookie.setPath(
                "/auth"
        );

        // 7 days
        cookie.setMaxAge(
                7 * 24 * 60 * 60
        );

        response.addCookie(
                cookie
        );

        log.debug(
                "Refresh token cookie created"
        );
    }


    // =========================================================
    // CLEAR REFRESH COOKIE
    // =========================================================

    private void clearRefreshCookie(
            HttpServletResponse response
    ) {

        Cookie cookie =
                new Cookie(
                        "refreshToken",
                        ""
                );

        cookie.setHttpOnly(true);

        cookie.setSecure(false);

        cookie.setPath(
                "/auth"
        );

        cookie.setMaxAge(0);

        response.addCookie(
                cookie
        );

        log.debug(
                "Refresh token cookie cleared"
        );
    }
}