package com.example.forgeHub.service;

import com.example.forgeHub.model.User;
import io.jsonwebtoken.Claims;

public interface JwtService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user, String jti);

    String generateTwoFactorSetupToken(User user);

    String generateTwoFactorLoginToken(User user);

    String generateTwoFactorRecoveryToken(User user);

    Claims extractClaims(String token);

    String extractUsername(String token);

    String extractJti(String token);

    String extractTokenType(String token);

    Integer extractUserId(String token);

    boolean isTokenExpired(String token);


}