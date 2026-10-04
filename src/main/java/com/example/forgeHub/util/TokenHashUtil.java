package com.example.forgeHub.util;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
@Slf4j
public class TokenHashUtil {

    // =========================================================
    // HASH TOKEN
    // =========================================================

    public String hash(String token) {

        if (token == null || token.isBlank()) {

            log.warn(
                    "Token hashing requested with null or empty token"
            );

            throw new IllegalArgumentException(
                    "Token cannot be null or empty"
            );
        }

        try {

            log.debug(
                    "Generating SHA-256 hash for token"
            );

            // -------------------------------------------------
            // STEP 1: Create SHA-256 digest
            // -------------------------------------------------

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            // -------------------------------------------------
            // STEP 2: Generate hash bytes
            // -------------------------------------------------

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            // -------------------------------------------------
            // STEP 3: Convert hash to hexadecimal
            // -------------------------------------------------

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hash) {

                hex.append(
                        String.format("%02x", b)
                );
            }

            String hashedToken =
                    hex.toString();

            log.debug(
                    "Token SHA-256 hashing completed successfully"
            );

            return hashedToken;

        } catch (NoSuchAlgorithmException e) {

            log.error(
                    "SHA-256 algorithm is not available",
                    e
            );

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }
}