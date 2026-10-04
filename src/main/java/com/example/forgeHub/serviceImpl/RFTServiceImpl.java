package com.example.forgeHub.serviceImpl;

import com.example.forgeHub.dto.request.RFTItemRequestDTO;
import com.example.forgeHub.dto.request.RFTRequestDTO;
import com.example.forgeHub.dto.response.RFTResponseDTO;
import com.example.forgeHub.exception.UserNotFoundException;
import com.example.forgeHub.mapper.RFTMapper;
import com.example.forgeHub.model.RFT;
import com.example.forgeHub.model.RFTItem;
import com.example.forgeHub.model.RFTVendor;
import com.example.forgeHub.model.User;
import com.example.forgeHub.repository.RFTRepository;
import com.example.forgeHub.repository.RFTVendorRepository;
import com.example.forgeHub.repository.UserRepository;
import com.example.forgeHub.service.RFTService;
import com.example.forgeHub.util.DocumentNumberGenerator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class RFTServiceImpl implements RFTService {

    private RFTRepository rftRepository;

    private UserRepository userRepository;

    private RFTVendorRepository rftVendorRepository;

    private DocumentNumberGenerator documentNumberGenerator;

    private RFTMapper rftMapper;


    @Transactional
    public RFTResponseDTO createRFT(
            RFTRequestDTO requestDTO,
            Long createdBy,
            List<Long> vendorIds) {


        // =========================================
        // 1. Generate RFT Number
        // =========================================

        String rftNo =
                documentNumberGenerator.generate("RFQ");


        // =========================================
        // 2. Generate Indent Number
        // =========================================

        String indentNo =
                documentNumberGenerator.generateIndentNumber("IND");


        // =========================================
        // 3. Find Logged-in User
        // =========================================

        User user =
                userRepository.findById(createdBy)
                        .orElseThrow(() ->
                                new UserNotFoundException(
                                        "User not found with ID: "
                                                + createdBy
                                )
                        );


        // =========================================
        // 4. Create RFT
        // =========================================

        RFT rft = RFT.builder()

                .rftNo(rftNo)

                .indentNo(indentNo)

                .description(
                        requestDTO.getDescription()
                )

                .deliveryLocation(
                        requestDTO.getDeliveryLocation()
                )

                .bidDate(
                        requestDTO.getBidDate()
                )

                .expiryDate(
                        requestDTO.getExpiryDate()
                )

                .createdBy(user)

                .build();


        // =========================================
        // 5. Create RFT Items
        // =========================================

        List<RFTItem> items = new ArrayList<>();


        if (requestDTO.getItems() != null) {

            for (RFTItemRequestDTO itemDTO :
                    requestDTO.getItems()) {


                RFTItem item = RFTItem.builder()

                        .rft(rft)

                        .lineNo(
                                Integer.valueOf(
                                        itemDTO.getLineNo()
                                )
                        )

                        .itemNo(
                                itemDTO.getItemNo()
                        )

                        .itemName(
                                itemDTO.getItemName()
                        )

                        .requiredQuantity(
                                itemDTO.getRequiredQuantity()
                        )

                        .uom(
                                itemDTO.getUom()
                        )

                        .requiredDeliveryDate(
                                itemDTO
                                        .getRequiredDeliveryDate()
                        )

                        .deliveryLocation(
                                itemDTO.getDeliveryLocation()
                        )

                        .description(
                                itemDTO.getDescription()
                        )

                        .factoryCode(
                                itemDTO.getFactoryCode()
                        )

                        .build();


                items.add(item);
            }
        }


        // =========================================
        // 6. Set Items into RFT
        // =========================================

        rft.setItems(items);


        // =========================================
        // 7. Save RFT
        // =========================================

        RFT savedRFT =
                rftRepository.save(rft);


        // =========================================
        // 8. Save Vendors
        // =========================================

        if (vendorIds != null &&
                !vendorIds.isEmpty()) {

            for (Long vendorId : vendorIds) {

                User vendor =
                        userRepository.findById(vendorId)
                                .orElseThrow(() ->
                                        new UserNotFoundException(
                                                "Vendor not found with ID: "
                                                        + vendorId
                                        )
                                );


                RFTVendor rftVendor =
                        RFTVendor.builder()
                                .rft(savedRFT)
                                .vendor(vendor)
                                .build();


                rftVendorRepository.save(rftVendor);
            }
        }


        return rftMapper.toResponse(savedRFT);
    }

    public List<RFTResponseDTO> getAllRFTs() {

        List<RFT> rfts =
                rftRepository.findAll();


        return rfts.stream()
                .map(rftMapper::toResponse)
                .toList();
    }

    public RFTResponseDTO getRFTById(Long id) {

        RFT rft =
                rftRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "RFT not found with ID: "
                                                + id
                                )
                        );


        return rftMapper.toResponse(rft);
    }

    @Transactional
    public void deleteRFT(Long id) {

        RFT rft =
                rftRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "RFT not found with ID: "
                                                + id
                                )
                        );


        rftRepository.delete(rft);
    }

    @Transactional
    public RFTResponseDTO updateRFT(
            Long id,
            RFTRequestDTO requestDTO) {


        RFT rft =
                rftRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "RFT not found"
                                )
                        );


        rft.setDescription(
                requestDTO.getDescription()
        );


        rft.setDeliveryLocation(
                requestDTO.getDeliveryLocation()
        );


        rft.setBidDate(
                requestDTO.getBidDate()
        );


        rft.setExpiryDate(
                requestDTO.getExpiryDate()
        );


        RFT updated =
                rftRepository.save(rft);


        return rftMapper.toResponse(updated);
    }

}