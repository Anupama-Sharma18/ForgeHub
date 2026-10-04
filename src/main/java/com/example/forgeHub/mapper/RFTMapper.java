package com.example.forgeHub.mapper;

import com.example.forgeHub.dto.response.RFTItemResponseDTO;
import com.example.forgeHub.dto.response.RFTResponseDTO;
import com.example.forgeHub.model.RFT;
import com.example.forgeHub.model.RFTItem;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RFTMapper {

    private final ModelMapper modelMapper;

    public RFTMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public RFTResponseDTO toResponse(RFT rft) {

        List<RFTItemResponseDTO> items =
                rft.getItems()
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return RFTResponseDTO.builder()
                .rftId(rft.getRftId())
                .rftNo(rft.getRftNo())
                .indentNo(rft.getIndentNo())
                .description(rft.getDescription())
                .deliveryLocation(rft.getDeliveryLocation())
                .bidDate(rft.getBidDate())
                .expiryDate(rft.getExpiryDate())
                .status(rft.getStatus())
                .createdBy(rft.getCreatedBy().getId())
                .items(items)
                .build();
    }

    public RFTItemResponseDTO toItemResponse(RFTItem item) {

        return modelMapper.map(
                item,
                RFTItemResponseDTO.class
        );
    }
}
