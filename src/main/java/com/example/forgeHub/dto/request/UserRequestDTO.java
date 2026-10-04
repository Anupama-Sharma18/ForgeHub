package com.example.forgeHub.dto.request;

import lombok.Data;

@Data
public class UserRequestDTO {

    private String fullName;

    private String email;

    private String password;

    private int isFirstTimeLogin;

    private String role;

    private String companyName;

    private String secretKey;

    private String mobileNo;

    private String address;

    private String gstNo;
}
