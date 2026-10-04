package com.example.forgeHub.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuotationItemRequestDTO {

    private Long rftItemId;

    private BigDecimal quantity;

    private BigDecimal unitPrice;

    private BigDecimal gstPercentage;

    private BigDecimal totalAmount;
}