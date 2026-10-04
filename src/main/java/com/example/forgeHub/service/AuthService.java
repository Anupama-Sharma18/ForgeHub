package com.example.forgeHub.service;

import com.example.forgeHub.dto.LoginResponse;
import com.example.forgeHub.dto.TokenResponse;
import jakarta.servlet.http.HttpServletResponse;
public interface AuthService {

    LoginResponse login(
            String email,
            String password,
            HttpServletResponse response
    );

    TokenResponse verify2FA(
            String token,
            String code,
            HttpServletResponse response
    );

    TokenResponse refresh(
            String refreshToken,
            HttpServletResponse response
    );

    void logout(
            String refreshToken,
            HttpServletResponse response
    );

    void sendRecoveryOtp(
            String email,
            String loginToken
    );

    LoginResponse verifyRecoveryOtp(
            String email,
            String otp,
            String loginToken,
            HttpServletResponse response
    );
}