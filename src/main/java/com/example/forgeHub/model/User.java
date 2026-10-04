package com.example.forgeHub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Users")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "FullName", nullable = false, columnDefinition = "LONGTEXT")
    private String fullName;

    @Column(name = "Email", nullable = false, columnDefinition = "LONGTEXT")
    private String email;

    @Column(name = "PasswordHash", nullable = false, columnDefinition = "LONGTEXT")
    private String passwordHash;

    @Column(name = "Role", nullable = false, columnDefinition = "LONGTEXT")
    private String role;

    @Column(name = "IsFirstTimeLogin", nullable = false)
    private Boolean isFirstTimeLogin=true;

    @Column(name = "SecretKey", columnDefinition = "LONGTEXT")
    private String secretKey;

    // Hashed refresh token
    @Column(name = "refresh_token_hash", columnDefinition = "LONGTEXT")
    private String refreshTokenHash;

    @Column(name = "refresh_jti", length = 36, unique = true)
    private String refreshJti;

    @Column(name = "revoked", nullable = false)
    private Boolean revoked = false;


}