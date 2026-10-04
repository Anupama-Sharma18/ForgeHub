package com.example.forgeHub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TwoFactorVerifyRequest {

    @NotBlank(message = "2FA code is required")
    @Pattern(
            regexp = "\\d{6}",
            message = "2FA code must be exactly 6 digits"
    )
    private String code;



}