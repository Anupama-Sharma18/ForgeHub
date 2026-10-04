package com.example.forgeHub.serviceImpl;

import com.example.forgeHub.dto.request.QuotationItemRequestDTO;
import com.example.forgeHub.dto.request.QuotationRequestDTO;
import com.example.forgeHub.dto.response.QuotationResponseDTO;
import com.example.forgeHub.model.Quotation;
import com.example.forgeHub.model.QuotationItem;
import com.example.forgeHub.model.RFT;
import com.example.forgeHub.model.RFTItem;
import com.example.forgeHub.model.User;
import com.example.forgeHub.repository.QuotationRepository;
import com.example.forgeHub.repository.RFTItemRepository;
import com.example.forgeHub.repository.RFTRepository;
import com.example.forgeHub.repository.UserRepository;
import com.example.forgeHub.util.DocumentNumberGenerator;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class QuotationServiceImpl {

    private final QuotationRepository quotationRepository;

    private final RFTRepository rftRepository;

    private final RFTItemRepository rftItemRepository;

    private final UserRepository userRepository;

    private final DocumentNumberGenerator documentNumberGenerator;


    // =====================================================
    // CREATE / UPDATE QUOTATION
    // =====================================================

    @Transactional
    public Quotation createQuotation(
            QuotationRequestDTO requestDTO,
            Long vendorId) {


        // ================================================
        // 1. VALIDATE VENDOR
        // ================================================

        User vendor =
                userRepository.findById(vendorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vendor not found"
                                )
                        );


        // ================================================
        // 2. VALIDATE RFT
        // ================================================

        RFT rft =
                rftRepository.findById(
                                requestDTO.getRftId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "RFT not found"
                                )
                        );


        // ================================================
        // 3. VALIDATE ITEMS
        // ================================================

        if (requestDTO.getQuotationItems() == null ||
                requestDTO.getQuotationItems().isEmpty()) {

            throw new RuntimeException(
                    "Quotation must contain at least one item"
            );
        }


        // ================================================
        // 4. CHECK EXISTING QUOTATION
        // ================================================

        Quotation quotation =
                quotationRepository
                        .findByRft_RftIdAndVendor_Id(
                                requestDTO.getRftId(),
                                vendorId
                        )
                        .orElse(null);


        // =================================================
        // 5. CREATE NEW QUOTATION
        // =================================================

        if (quotation == null) {

            String bidNo =
                    documentNumberGenerator.generate("BID");


            quotation =
                    Quotation.builder()
                            .bidNo(bidNo)
                            .rft(rft)
                            .vendor(vendor)
                            .status("SUBMITTED")
                            .submittedDate(
                                    LocalDateTime.now()
                            )
                            .quotationItems(
                                    new ArrayList<>()
                            )
                            .build();

        }

        // =================================================
        // 6. EXISTING QUOTATION
        // =================================================

        else {

            // APPROVED quotation cannot be changed

            if ("APPROVED".equals(
                    quotation.getStatus())) {

                throw new RuntimeException(
                        "Approved quotation cannot be modified"
                );
            }


            // Keep same bid number

            quotation.setRft(rft);

            quotation.setVendor(vendor);

            quotation.setStatus("SUBMITTED");

            quotation.setSubmittedDate(
                    LocalDateTime.now()
            );


            // Remove old items

            quotation
                    .getQuotationItems()
                    .clear();
        }


        // =================================================
        // 7. UPDATE HEADER DATA
        // =================================================

        quotation.setDeliveryDate(
                requestDTO.getDeliveryDate()
        );

        quotation.setPaymentTerms(
                requestDTO.getPaymentTerms()
        );

        quotation.setRemarks(
                requestDTO.getRemarks()
        );


        // =================================================
        // 8. CREATE QUOTATION ITEMS
        // =================================================

        for (
                QuotationItemRequestDTO itemDTO
                : requestDTO.getQuotationItems()
        ) {


            // ---------------------------------------------
            // Find RFT Item
            // ---------------------------------------------

            RFTItem rftItem =
                    rftItemRepository.findById(
                                    itemDTO.getRftItemId()
                            )
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "RFT Item not found: "
                                                    + itemDTO.getRftItemId()
                                    )
                            );


            // ---------------------------------------------
            // Quantity
            // ---------------------------------------------

            BigDecimal quantity =
                    itemDTO.getQuantity();


            if (quantity == null ||
                    quantity.compareTo(
                            BigDecimal.ZERO
                    ) <= 0) {

                throw new RuntimeException(
                        "Quantity must be greater than zero"
                );
            }


            // ---------------------------------------------
            // Unit Price
            // ---------------------------------------------

            BigDecimal unitPrice =
                    itemDTO.getUnitPrice();


            if (unitPrice == null ||
                    unitPrice.compareTo(
                            BigDecimal.ZERO
                    ) < 0) {

                throw new RuntimeException(
                        "Unit price cannot be negative"
                );
            }


            // ---------------------------------------------
            // GST = 18%
            // ---------------------------------------------

            BigDecimal gstPercentage =
                    new BigDecimal("18.00");


            // ---------------------------------------------
            // SUBTOTAL
            // ---------------------------------------------

            BigDecimal subtotal =
                    quantity
                            .multiply(unitPrice)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );


            // ---------------------------------------------
            // GST AMOUNT
            // ---------------------------------------------

            BigDecimal gstAmount =
                    subtotal
                            .multiply(gstPercentage)
                            .divide(
                                    new BigDecimal("100"),
                                    2,
                                    RoundingMode.HALF_UP
                            );


            // ---------------------------------------------
            // GRAND TOTAL
            // ---------------------------------------------

            BigDecimal totalAmount =
                    subtotal
                            .add(gstAmount)
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            );


            // ---------------------------------------------
            // CREATE ITEM
            // ---------------------------------------------

            QuotationItem quotationItem =
                    QuotationItem.builder()
                            .quotation(quotation)
                            .rftItem(rftItem)
                            .quantity(quantity)
                            .unitPrice(unitPrice)
                            .gstPercentage(
                                    gstPercentage
                            )
                            .totalAmount(
                                    totalAmount
                            )
                            .build();


            quotation
                    .getQuotationItems()
                    .add(quotationItem);
        }


        // =================================================
        // 9. SAVE DATABASE
        // =================================================

        return quotationRepository.save(
                quotation
        );
    }


    // =====================================================
    // APPROVE QUOTATION
    // =====================================================

    @Transactional
    public void approveQuotation(
            Long quotationId) {


        Quotation quotation =
                quotationRepository.findById(
                                quotationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quotation not found"
                                )
                        );


        if ("APPROVED".equals(
                quotation.getStatus())) {

            throw new RuntimeException(
                    "Quotation is already approved"
            );
        }


        quotation.setStatus("APPROVED");


        RFT rft=quotation.getRft();

        rft.setStatus("CLOSE");

        rftRepository.save(rft);

        quotationRepository.save(
                quotation
        );
    }


    // =====================================================
    // REJECT QUOTATION
    // =====================================================

    @Transactional
    public void rejectQuotation(
            Long quotationId) {


        Quotation quotation =
                quotationRepository.findById(
                                quotationId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quotation not found"
                                )
                        );


        if ("APPROVED".equals(
                quotation.getStatus())) {

            throw new RuntimeException(
                    "Approved quotation cannot be rejected"
            );
        }


        quotation.setStatus("REJECTED");


        quotationRepository.save(
                quotation
        );
    }


    // =====================================================
    // GET ALL QUOTATIONS
    // =====================================================

    @Transactional(readOnly = true)
    public List<QuotationResponseDTO>
    getAllQuotations() {


        List<Quotation> quotations =
                quotationRepository.findAll();


        return quotations
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }


    // =====================================================
    // GET QUOTATION BY ID
    // =====================================================

    @Transactional(readOnly = true)
    public Quotation getQuotationById(
            Long quotationId) {

        return quotationRepository
                .findById(quotationId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Quotation not found"
                        )
                );
    }


    // =====================================================
    // MAP RESPONSE
    // =====================================================

    private QuotationResponseDTO
    mapToResponseDTO(
            Quotation quotation) {


        return QuotationResponseDTO
                .builder()

                .quotationId(
                        quotation.getQuotationId()
                )

                .bidNo(
                        quotation.getBidNo()
                )

                .rftId(
                        quotation
                                .getRft()
                                .getRftId()
                )

                .vendorId(
                        quotation
                                .getVendor()
                                .getId()
                )

                .vendorName(
                        quotation
                                .getVendor()
                                .getFullName()
                )

                .companyName(
                        quotation
                                .getVendor()
                                .getCompanyName()
                )

                .deliveryDate(
                        quotation
                                .getDeliveryDate()
                )

                .paymentTerms(
                        quotation
                                .getPaymentTerms()
                )

                .remarks(
                        quotation
                                .getRemarks()
                )

                .status(
                        quotation
                                .getStatus()
                )

                .submittedDate(
                        quotation
                                .getSubmittedDate()
                )

                .build();
    }

    // =====================================================
// GET VENDOR QUOTATIONS
// =====================================================

    @Transactional(readOnly = true)
    public List<QuotationResponseDTO> getVendorQuotations(
            Long vendorId) {

        List<Quotation> quotations =
                quotationRepository
                        .findByVendor_Id(vendorId);

        return quotations
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Quotation getQuotationForVendor(
            Long quotationId,
            Long vendorId) {

        Quotation quotation =
                quotationRepository
                        .findById(quotationId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Quotation not found"
                                )
                        );

        // IMPORTANT SECURITY CHECK
        if (!quotation.getVendor()
                .getId()
                .equals(vendorId)) {

            throw new RuntimeException(
                    "You are not authorized to view this quotation"
            );
        }

        // Load quotation items
        quotation.getQuotationItems().size();

        // Load RFT items
        quotation.getRft().getItems().size();

        return quotation;
    }
}