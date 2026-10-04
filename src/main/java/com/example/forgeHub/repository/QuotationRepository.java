package com.example.forgeHub.repository;

import com.example.forgeHub.model.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuotationRepository
        extends JpaRepository<Quotation, Long> {

    Optional<Quotation> findByRft_RftIdAndVendor_Id(
            Long rftId,
            Long vendorId
    );

    List<Quotation> findByVendor_Id(Long vendorId);


}