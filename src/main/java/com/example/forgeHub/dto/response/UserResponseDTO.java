package com.example.forgeHub.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {

    private Long id;

    private String fullName;

    private String email;

    private int isFirstTimeLogin;

    private String role;

    private String companyName;

    private String mobileNo;

    private String address;

    private String gstNo;
}
