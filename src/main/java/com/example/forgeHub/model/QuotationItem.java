package com.example.forgeHub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuotationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quotation_item_id")
    private Long quotationItemId;


    // ==============================
    // QUOTATION
    // ==============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "quotation_id",
            referencedColumnName = "quotation_id",
            nullable = false
    )
    private Quotation quotation;


    // ==============================
    // RFT ITEM
    // ==============================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "rft_item_id",
            referencedColumnName = "item_id",
            nullable = false
    )
    private RFTItem rftItem;


    // ==============================
    // QUANTITY
    // ==============================

    @Column(
            name = "quantity",
            precision = 18,
            scale = 3,
            nullable = false
    )
    private BigDecimal quantity;


    // ==============================
    // UNIT PRICE
    // ==============================

    @Column(
            name = "unit_price",
            precision = 18,
            scale = 2,
            nullable = false
    )
    private BigDecimal unitPrice;


    // ==============================
    // GST
    // ==============================

    @Column(
            name = "gst_percentage",
            precision = 5,
            scale = 2,
            nullable = false
    )
    private BigDecimal gstPercentage;


    // ==============================
    // TOTAL
    // ==============================

    @Column(
            name = "total_amount",
            precision = 18,
            scale = 2,
            nullable = false
    )
    private BigDecimal totalAmount;
}