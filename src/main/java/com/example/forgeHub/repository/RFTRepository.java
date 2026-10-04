package com.example.forgeHub.repository;

import com.example.forgeHub.model.RFT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RFTRepository extends JpaRepository<RFT,Long> {
}
