package com.example.forgeHub.util;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "number_sequence",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"document_type", "year_value", "reference_code"}
                )
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NumberSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_type", nullable = false, length = 20)
    private String documentType;

    @Column(name = "year_value", nullable = false)
    private Integer year;

    @Column(name = "reference_code", nullable = false, length = 20)
    private String referenceCode;

    @Column(name = "last_number", nullable = false)
    private Long lastNumber;
}