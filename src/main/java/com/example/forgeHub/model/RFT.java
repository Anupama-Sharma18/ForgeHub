package com.example.forgeHub.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RFT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rftId;

    @Column(nullable = false, unique = true, length = 100)
    private String rftNo;

    @Column(nullable = false,length = 100)
    private String indentNo;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String deliveryLocation;

    private LocalDateTime bidDate;

    private LocalDateTime expiryDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RFTStatus status= RFTStatus.OPEN;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;


}
