package com.example.forgeHub.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RFTRequestDTO {

    private String description;

    private String deliveryLocation;

    private LocalDateTime bidDate;

    private LocalDateTime expiryDate;

    private String status;

    private Long createdBy;

    private List<RFTItemRequestDTO> items;
}