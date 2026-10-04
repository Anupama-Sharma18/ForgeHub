package com.example.forgeHub.repository;

import com.example.forgeHub.model.RFTVendor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RFTVendorRepository
        extends JpaRepository<RFTVendor, Long> {

    List<RFTVendor> findByVendor_Id(Long vendorId);
}