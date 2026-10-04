package com.example.forgeHub.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class QuotationItemResponseDTO {

    private Long quotationItemId;

    private Long rftItemId;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal gstPercentage;

    private BigDecimal totalAmount;
}
