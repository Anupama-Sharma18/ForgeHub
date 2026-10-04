package com.example.forgeHub.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuotationRequestDTO {

    private Long rftId;

    private Long vendorId;

    private LocalDateTime deliveryDate;

    private String paymentTerms;

    private String remarks;

    private String status;

    private LocalDateTime submittedDate;

    private List<QuotationItemRequestDTO> quotationItems;
}