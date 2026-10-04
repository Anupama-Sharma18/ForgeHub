package com.example.forgeHub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RFTItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    // Many RFTItems -> One RFT
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rft_id",
    referencedColumnName = "rft_id",nullable = false)
    private RFT rft;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "item_no", length = 50, nullable = false)
    private String itemNo;

    @Column(name = "item_name", length = 200, nullable = false)
    private String itemName;

    @Column(name = "required_quantity", precision = 18, scale = 3, nullable = false)
    private BigDecimal requiredQuantity;

    @Column(name = "uom", length = 30, nullable = false)
    private String uom;

    @Column(name = "required_delivery_date", nullable = false)
    private LocalDateTime requiredDeliveryDate;

    @Column(name = "delivery_location", length = 255)
    private String deliveryLocation;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "factory_code", length = 50)
    private String factoryCode;

    // RFTItem -> QuotationItem
    @OneToMany(mappedBy = "rftItem")
    private List<QuotationItem> quotationItems = new ArrayList<>();


}
