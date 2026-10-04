package com.example.forgeHub.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RFTVendorResponseDTO {

    private Long id;

    private Long rftId;

    private Long vendorId;

    private String vendorName;

    private String vendorCompanyName;
}
