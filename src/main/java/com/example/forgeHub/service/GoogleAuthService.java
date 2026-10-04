package com.example.forgeHub.service;

public interface GoogleAuthService {

    // Generate new Google Authenticator secret
    String generateSecret();

    // Verify 6-digit OTP
    boolean verifyCode(
            String secretKey,
            int code
    );

    // Generate OTP Auth URL for QR code
    String generateQrUrl(
            String email,
            String secretKey
    );
}