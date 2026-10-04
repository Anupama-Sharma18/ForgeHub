package com.example.forgeHub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class QuotationResponseDTO {

    private Long quotationId;

    private String bidNo;

    private Long rftId;

    private Long vendorId;

    private String vendorName;

    private String companyName;

    private LocalDateTime deliveryDate;

    private String paymentTerms;

    private String remarks;

    private String status;

    private LocalDateTime submittedDate;

    private List<QuotationItemResponseDTO> quotationItems;
}