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
public class RFT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rft_id")
    private Long rftId;

    @Column(nullable = false, unique = true, length = 100, name =  "rft_no")
    private String rftNo;

    @Column(nullable = false,length = 100, name = "indent_no")
    private String indentNo;

    @Column(columnDefinition = "TEXT", name = "description")
    private String description;

    @Column(length = 255, name = "delivery_location")
    private String deliveryLocation;

    @Column(name = "bid_date")
    private LocalDateTime bidDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Builder.Default
    @Column(
            nullable = false,
            columnDefinition = "ENUM('DRAFT','OPEN','CLOSE')"
    )
    private String status = "OPEN";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by",
            nullable = false, referencedColumnName = "user_id")
    private User createdBy;

    @OneToMany(
            mappedBy = "rft",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RFTItem> items = new ArrayList<>();

    @OneToMany(
            mappedBy = "rft",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<RFTVendor> vendors = new ArrayList<>();

    @OneToMany(mappedBy = "rft")
    private List<Quotation> quotations;

}
