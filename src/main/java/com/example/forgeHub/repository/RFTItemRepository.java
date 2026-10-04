package com.example.forgeHub.repository;

import com.example.forgeHub.model.RFTItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RFTItemRepository
        extends JpaRepository<RFTItem, Long> {

    List<RFTItem> findByRftRftId(Long rftId);
}