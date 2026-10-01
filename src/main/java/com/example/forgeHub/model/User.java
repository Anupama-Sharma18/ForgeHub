package com.example.forgeHub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String fullName;

    @Column(nullable = false , unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String password;

    private int IsFirstTimeLogin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 100)
    private String companyName;

    @Column(length = 300)
    private String SecretKey;

    @Column(unique = true, length = 10)
    private String mobileNo;

    @Column(length = 300)
    private String address;

    @Column(unique = true, length = 30)
    private String gstNo;


}
