package com.example.forgeHub.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RFTResponseDTO {

    private Long rftId;

    private String rftNo;

    private String indentNo;

    private String description;

    private String deliveryLocation;

    private LocalDateTime bidDate;

    private LocalDateTime expiryDate;

    private String status;

    private Long createdBy;

    private List<RFTItemResponseDTO> items;
}