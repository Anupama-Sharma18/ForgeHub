package com.example.forgeHub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Quotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_id")
    private Long quotationId;


    @Column(name = "bid_no", length = 50)
    private String bidNo;


    // ==============================
    // QUOTATION -> RFT
    // ==============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "rft_id",
            referencedColumnName = "rft_id",
            nullable = false
    )
    private RFT rft;


    // ==============================
    // QUOTATION -> VENDOR
    // ==============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            referencedColumnName = "user_id",
            nullable = false
    )
    private User vendor;


    // ==============================
    // DELIVERY
    // ==============================

    @Column(
            name = "delivery_date",
            nullable = false
    )
    private LocalDateTime deliveryDate;


    // ==============================
    // PAYMENT
    // ==============================

    @Column(
            name = "payment_terms",
            length = 500
    )
    private String paymentTerms;


    // ==============================
    // REMARKS
    // ==============================

    @Column(
            name = "remarks",
            columnDefinition = "TEXT"
    )
    private String remarks;


    // ==============================
    // STATUS
    // ==============================

    @Column(
            name = "status",
            nullable = false,
            columnDefinition =
                    "ENUM('SUBMITTED','APPROVED','REJECTED')"
    )
    private String status;


    // ==============================
    // SUBMITTED DATE
    // ==============================

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;


    // ==============================
    // QUOTATION ITEMS
    // ==============================

    @OneToMany(
            mappedBy = "quotation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<QuotationItem> quotationItems =
            new ArrayList<>();
}