package com.example.forgeHub.repository;

import com.example.forgeHub.util.NumberSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NumberSequenceRepository
        extends JpaRepository<NumberSequence, Long> {

    Optional<NumberSequence> findByDocumentTypeAndYearAndReferenceCode(
            String documentType,
            Integer year,
            String referenceCode
    );
}