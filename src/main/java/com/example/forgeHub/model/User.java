package com.example.forgeHub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    // =========================================================
    // PRIMARY KEY
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "User_id")
    @EqualsAndHashCode.Include
    private Long id;


    // =========================================================
    // COMMON / AUTHENTICATION FIELDS
    // =========================================================

    @Column(
            name = "FullName",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String fullName;


    @Column(
            name = "Email",
            nullable = false,
            unique = true,
            columnDefinition = "LONGTEXT"
    )
    private String email;


    @Column(
            name = "PasswordHash",
            nullable = false,
            columnDefinition = "LONGTEXT"
    )
    private String passwordHash;


    @Column(
            name = "Role",
            nullable = false,
            columnDefinition = "ENUM('ADMIN','VENDOR')"
    )
    private String role;


    // =========================================================
    // FIRST-TIME LOGIN / 2FA
    // =========================================================

    @Column(
            name = "IsFirstTimeLogin",
            nullable = false
    )
    @Builder.Default
    private Boolean isFirstTimeLogin = true;


    @Lob
    @Column(
            name = "SecretKey",
            columnDefinition = "LONGTEXT"
    )
    private String secretKey;


    // =========================================================
    // REFRESH TOKEN / JWT
    // =========================================================

    // Hashed refresh token
    @Column(
            name = "refresh_token_hash",
            columnDefinition = "LONGTEXT"
    )
    private String refreshTokenHash;


    // JTI of refresh token
    @Column(
            name = "refresh_jti",
            length = 36,
            unique = true
    )
    private String refreshJti;


    // Refresh token revoked or not
    @Column(
            name = "revoked",
            nullable = false
    )
    @Builder.Default
    private Boolean revoked = false;


    // =========================================================
    // RFQ / VENDOR DETAILS
    // =========================================================

    @Column(
            name = "company_name",
            length = 100
    )
    private String companyName;


    @Column(
            name = "mobile",
            unique = true,
            length = 10
    )
    private String mobileNo;


    @Column(
            name = "address",
            length = 300
    )
    private String address;


    @Column(
            name = "gst_number",
            unique = true,
            length = 30
    )
    private String gstNo;


    // =========================================================
    // RFQ RELATIONSHIPS
    // =========================================================

    // User -> RFT
    @OneToMany(mappedBy = "createdBy")
    @Builder.Default
    private List<RFT> createdRfts = new ArrayList<>();


    // User -> RFTVendor
    @OneToMany(mappedBy = "vendor")
    @Builder.Default
    private List<RFTVendor> rftVendors = new ArrayList<>();


    // User -> Quotation
    @OneToMany(mappedBy = "vendor")
    @Builder.Default
    private List<Quotation> quotations = new ArrayList<>();
}
