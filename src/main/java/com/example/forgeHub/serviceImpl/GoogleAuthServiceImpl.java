package com.example.forgeHub.serviceImpl;

import com.example.forgeHub.service.GoogleAuthService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final GoogleAuthenticator googleAuthenticator;

    public GoogleAuthServiceImpl() {
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    // =========================================================
    // Generate Google Authenticator Secret Key
    // =========================================================
    @Override
    public String generateSecret() {

        GoogleAuthenticatorKey credentials =
                googleAuthenticator.createCredentials();

        return credentials.getKey();
    }

    // =========================================================
    // Verify 6 Digit OTP
    // =========================================================
    @Override
    public boolean verifyCode(
            String secretKey,
            int code
    ) {

        return googleAuthenticator.authorize(
                secretKey,
                code
        );
    }

    // =========================================================
    // Generate QR Code URL
    // =========================================================
    @Override
    public String generateQrUrl(
            String email,
            String secretKey
    ) {

        GoogleAuthenticatorKey credentials =
                new GoogleAuthenticatorKey.Builder(secretKey)
                        .setKey(secretKey)
                        .build();

        return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                "ForgeHub",
                email,
                credentials
        );
    }
}