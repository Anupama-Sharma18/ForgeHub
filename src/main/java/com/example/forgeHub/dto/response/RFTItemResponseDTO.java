package com.example.forgeHub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RFTItemResponseDTO {

    private Long itemId;

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