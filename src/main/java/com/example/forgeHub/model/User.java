package com.example.forgeHub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 100, nullable = false, name = "full_name")
    private String fullName;

    @Column(nullable = false , unique = true, length = 100, name = "email")
    private String email;

    @Column(nullable = false, length = 20, name = "password")
    private String password;

    @Column(nullable = false, name = "IsFirstTimeLogin")
    private int IsFirstTimeLogin;

    @Column(
            nullable = false,
            columnDefinition = "ENUM('ADMIN','VENDOR')"
    )
    private String role;

    @Column(length = 100,name = "company_name")
    private String companyName;

    @Lob
    @Column(columnDefinition = "LONGTEXT", name = "SecretKey")
    private String SecretKey;

    @Column(unique = true, length = 10, name = "mobile")
    private String mobileNo;

    @Column(length = 300,name = "address")
    private String address;

    @Column(unique = true, length = 30, name = "gst_number")
    private String gstNo;

    // User -> RFT
    @OneToMany(mappedBy = "createdBy")
    private List<RFT> createdRfts = new ArrayList<>();

    // User -> RFTVendor
    @OneToMany(mappedBy = "vendor")
    private List<RFTVendor> rftVendors = new ArrayList<>();

    // User -> Quotation
    @OneToMany(mappedBy = "vendor")
    private List<Quotation> quotations = new ArrayList<>();


}
