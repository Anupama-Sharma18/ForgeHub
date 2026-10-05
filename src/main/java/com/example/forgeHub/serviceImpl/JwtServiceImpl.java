package com.example.forgeHub.serviceImpl;

import com.example.forgeHub.model.User;
import com.example.forgeHub.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    // =========================================================
    // JWT SECRET
    // =========================================================

    @Value("${jwt.secret}")
    private String secret;


    // =========================================================
    // TOKEN EXPIRATION
    // =========================================================

    @Value("${jwt.access-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.two-factor-setup-expiration}")
    private long twoFactorSetupExpiration;

    @Value("${jwt.two-factor-login-expiration}")
    private long twoFactorLoginExpiration;


    // =========================================================
    // SECRET KEY
    // =========================================================

    private SecretKey getSecretKey() {

        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secret)
        );
    }


    // =========================================================
    // ACCESS TOKEN
    // =========================================================

    @Override
    public String generateAccessToken(User user) {

        log.debug(
                "Generating ACCESS token for userId={}",
                user.getId()
        );

        String token =
                Jwts.builder()
                        .subject(user.getEmail())
                        .claim("userId", user.getId())
                        .claim("role", user.getRole())
                        .claim("type", "ACCESS")
                        .issuedAt(new Date())
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + accessTokenExpiration
                                )
                        )
                        .signWith(getSecretKey())
                        .compact();

        log.debug(
                "ACCESS token generated successfully for userId={}",
                user.getId()
        );

        // NEVER log the actual token
        return token;
    }


    // =========================================================
    // 2FA LOGIN TOKEN
    // =========================================================

    @Override
    public String generateTwoFactorLoginToken(User user) {

        log.info(
                "Generating temporary 2FA_LOGIN token for userId={}",
                user.getId()
        );

        String token =
                Jwts.builder()
                        .subject(user.getEmail())
                        .claim("userId", user.getId())
                        .claim("type", "2FA_LOGIN")
                        .issuedAt(new Date())
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + twoFactorLoginExpiration
                                )
                        )
                        .signWith(getSecretKey())
                        .compact();

        log.debug(
                "2FA_LOGIN token generated successfully for userId={} with expiration={} ms",
                user.getId(),
                twoFactorLoginExpiration
        );

        // NEVER log the actual token
        return token;
    }

    // =========================================================
// 2FA RECOVERY TOKEN
// =========================================================

    @Override
    public String generateTwoFactorRecoveryToken(User user) {

        log.info(
                "Generating temporary 2FA_RECOVERY token for userId={}",
                user.getId()
        );

        String token =
                Jwts.builder()
                        .subject(user.getEmail())
                        .claim("userId", user.getId())
                        .claim("type", "2FA_RECOVERY")
                        .issuedAt(new Date())
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + twoFactorLoginExpiration
                                )
                        )
                        .signWith(getSecretKey())
                        .compact();

        log.debug(
                "2FA_RECOVERY token generated successfully for userId={}",
                user.getId()
        );

        return token;
    }


    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @Override
    public String generateRefreshToken(
            User user,
            String jti
    ) {

        log.info(
                "Generating REFRESH token for userId={} with new JTI",
                user.getId()
        );

        String token =
                Jwts.builder()
                        .subject(user.getEmail())
                        .claim("userId", user.getId())
                        .claim("type", "REFRESH")
                        .id(jti)
                        .issuedAt(new Date())
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + refreshTokenExpiration
                                )
                        )
                        .signWith(getSecretKey())
                        .compact();

        log.debug(
                "REFRESH token generated successfully for userId={}",
                user.getId()
        );

        // NEVER log token or JTI
        return token;
    }


    // =========================================================
    // 2FA SETUP TOKEN
    // =========================================================

    @Override
    public String generateTwoFactorSetupToken(User user) {

        log.info(
                "Generating temporary 2FA_SETUP token for userId={}",
                user.getId()
        );

        String token =
                Jwts.builder()
                        .subject(user.getEmail())
                        .claim("userId", user.getId())
                        .claim("type", "2FA_SETUP")
                        .issuedAt(new Date())
                        .expiration(
                                new Date(
                                        System.currentTimeMillis()
                                                + twoFactorSetupExpiration
                                )
                        )
                        .signWith(getSecretKey())
                        .compact();

        log.debug(
                "2FA_SETUP token generated successfully for userId={} with expiration={} ms",
                user.getId(),
                twoFactorSetupExpiration
        );

        // NEVER log the actual token
        return token;
    }


    // =========================================================
    // EXTRACT CLAIMS
    // =========================================================

    @Override
    public Claims extractClaims(String token) {

        try {

            Claims claims =
                    Jwts.parser()
                            .verifyWith(getSecretKey())
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

            log.debug(
                    "JWT claims parsed and signature verified successfully"
            );

            return claims;

        } catch (JwtException | IllegalArgumentException e) {

            log.warn(
                    "JWT validation failed: {}",
                    e.getMessage()
            );

            throw e;
        }
    }


    // =========================================================
    // EXTRACT USERNAME
    // =========================================================

    @Override
    public String extractUsername(String token) {

        String username =
                extractClaims(token)
                        .getSubject();

        log.debug(
                "Username/subject extracted from JWT"
        );

        return username;
    }


    // =========================================================
    // EXTRACT JTI
    // =========================================================

    @Override
    public String extractJti(String token) {

        String jti =
                extractClaims(token)
                        .getId();

        log.debug(
                "JTI extracted from JWT"
        );

        return jti;
    }


    // =========================================================
    // EXTRACT TOKEN TYPE
    // =========================================================

    @Override
    public String extractTokenType(String token) {

        String tokenType =
                extractClaims(token)
                        .get("type", String.class);

        log.debug(
                "JWT token type extracted: {}",
                tokenType
        );

        return tokenType;
    }


    // =========================================================
    // EXTRACT USER ID
    // =========================================================

    @Override
    public Integer extractUserId(String token) {

        Integer userId =
                extractClaims(token)
                        .get("userId", Integer.class);

        log.debug(
                "User ID extracted from JWT: {}",
                userId
        );

        return userId;
    }


    // =========================================================
    // CHECK TOKEN EXPIRATION
    // =========================================================

    @Override
    public boolean isTokenExpired(String token) {

        Date expiration =
                extractClaims(token)
                        .getExpiration();

        boolean expired =
                expiration.before(new Date());

        log.debug(
                "JWT expiration checked. expired={}",
                expired
        );

        return expired;
    }
}