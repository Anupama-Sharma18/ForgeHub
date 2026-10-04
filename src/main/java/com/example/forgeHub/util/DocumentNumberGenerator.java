package com.example.forgeHub.util;

import com.example.forgeHub.repository.NumberSequenceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
@RequiredArgsConstructor
public class DocumentNumberGenerator {

    private final NumberSequenceRepository numberSequenceRepository;

    @Transactional
    public String generate(String documentType) {

        return generate(documentType, "DEFAULT");
    }

    @Transactional
    public String generate(String documentType, String referenceCode) {

        int currentYear = Year.now().getValue();
        String yearCode = String.valueOf(currentYear).substring(2);

        NumberSequence sequence =
                numberSequenceRepository
                        .findByDocumentTypeAndYearAndReferenceCode(
                                documentType,
                                currentYear,
                                referenceCode
                        )
                        .orElseGet(() -> NumberSequence.builder()
                                .documentType(documentType)
                                .year(currentYear)
                                .referenceCode(referenceCode)
                                .lastNumber(0L)
                                .build());

        long nextNumber = sequence.getLastNumber() + 1;

        sequence.setLastNumber(nextNumber);

        numberSequenceRepository.save(sequence);

        return String.format(
                "%s/%s/%06d",
                documentType,
                yearCode,
                nextNumber
        );
    }

    @Transactional
    public String generateIndentNumber(String factoryCode) {

        int currentYear = Year.now().getValue();
        String yearCode = String.valueOf(currentYear).substring(2);

        NumberSequence sequence =
                numberSequenceRepository
                        .findByDocumentTypeAndYearAndReferenceCode(
                                "IND",
                                currentYear,
                                factoryCode
                        )
                        .orElseGet(() -> NumberSequence.builder()
                                .documentType("IND")
                                .year(currentYear)
                                .referenceCode(factoryCode)
                                .lastNumber(0L)
                                .build());

        long nextNumber = sequence.getLastNumber() + 1;

        sequence.setLastNumber(nextNumber);

        numberSequenceRepository.save(sequence);

        return String.format(
                "IND/%s/%s/%04d",
                yearCode,
                factoryCode,
                nextNumber
        );
    }
}