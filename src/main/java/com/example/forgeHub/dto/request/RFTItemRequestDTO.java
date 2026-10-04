package com.example.forgeHub.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RFTItemRequestDTO {

    private Integer lineNo;

    private String itemNo;

    private String itemName;

    private BigDecimal requiredQuantity;

    private String uom;

    private LocalDateTime requiredDeliveryDate;

    private String deliveryLocation;

    private String description;

    private String factoryCode;
}