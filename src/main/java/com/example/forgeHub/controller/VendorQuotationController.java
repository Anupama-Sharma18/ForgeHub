package com.example.forgeHub.controller;

import com.example.forgeHub.dto.request.QuotationRequestDTO;
import com.example.forgeHub.dto.response.QuotationResponseDTO;
import com.example.forgeHub.model.Quotation;
import com.example.forgeHub.model.RFT;
import com.example.forgeHub.model.RFTVendor;
import com.example.forgeHub.repository.RFTVendorRepository;
import com.example.forgeHub.serviceImpl.QuotationServiceImpl;

import jakarta.servlet.http.HttpSession;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/vendor/quotation")
@AllArgsConstructor
public class VendorQuotationController {

    private final RFTVendorRepository rftVendorRepository;

    private final QuotationServiceImpl quotationService;


    // =====================================================
    // CREATE QUOTATION PAGE
    // =====================================================

    @GetMapping("/create/{rftId}")
    public String showCreateQuotationPage(

            @PathVariable Long rftId,

            HttpSession session,

            Model model) {

        // -----------------------------------------
        // CHECK LOGIN
        // -----------------------------------------

        Long vendorId =
                (Long) session.getAttribute("userId");

        if (vendorId == null) {
            return "redirect:/auth/login";
        }


        // -----------------------------------------
        // CHECK RFT ASSIGNMENT
        // -----------------------------------------

        RFTVendor assignment =
                rftVendorRepository
                        .findByVendor_Id(vendorId)
                        .stream()
                        .filter(x ->
                                x.getRft()
                                        .getRftId()
                                        .equals(rftId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "This RFT is not assigned to this vendor"
                                )
                        );


        // -----------------------------------------
        // GET RFT
        // -----------------------------------------

        RFT rft = assignment.getRft();

        // Load RFT items
        rft.getItems().size();


        // -----------------------------------------
        // SEND DATA TO THYMELEAF
        // -----------------------------------------

        model.addAttribute(
                "rft",
                rft
        );

        model.addAttribute(
                "quotationRequestDTO",
                new QuotationRequestDTO()
        );


        return "vendor/quotation-create";
    }


    // =====================================================
    // SUBMIT QUOTATION
    // =====================================================

    @PostMapping("/create/{rftId}")
    public String createQuotation(

            @PathVariable Long rftId,

            @ModelAttribute("quotationRequestDTO")
            QuotationRequestDTO requestDTO,

            HttpSession session,

            Model model) {


        // -----------------------------------------
        // CHECK LOGIN
        // -----------------------------------------

        Long vendorId =
                (Long) session.getAttribute("userId");

        if (vendorId == null) {
            return "redirect:/auth/login";
        }


        try {

            // -----------------------------------------
            // CHECK RFT ASSIGNMENT
            // -----------------------------------------

            RFTVendor assignment =
                    rftVendorRepository
                            .findByVendor_Id(vendorId)
                            .stream()
                            .filter(x ->
                                    x.getRft()
                                            .getRftId()
                                            .equals(rftId)
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "This RFT is not assigned to this vendor"
                                    )
                            );


            // -----------------------------------------
            // SET RFT ID
            // -----------------------------------------

            requestDTO.setRftId(rftId);


            // -----------------------------------------
            // SET VENDOR ID
            // -----------------------------------------

            requestDTO.setVendorId(vendorId);


            // -----------------------------------------
            // SAVE QUOTATION TO DATABASE
            // -----------------------------------------

            quotationService.createQuotation(
                    requestDTO,
                    vendorId
            );


            // -----------------------------------------
            // SUCCESS REDIRECT
            // -----------------------------------------

            return "redirect:/vendor/quotation/list?submitted=true";


        } catch (Exception e) {

            e.printStackTrace();


            // -----------------------------------------
            // ERROR MESSAGE
            // -----------------------------------------

            model.addAttribute(
                    "errorMessage",
                    "Failed to submit quotation: "
                            + e.getMessage()
            );


            // -----------------------------------------
            // LOAD RFT AGAIN
            // -----------------------------------------

            RFT rft =
                    rftVendorRepository
                            .findByVendor_Id(vendorId)
                            .stream()
                            .filter(x ->
                                    x.getRft()
                                            .getRftId()
                                            .equals(rftId)
                            )
                            .findFirst()
                            .map(RFTVendor::getRft)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "RFT not found"
                                    )
                            );


            rft.getItems().size();


            model.addAttribute(
                    "rft",
                    rft
            );


            model.addAttribute(
                    "quotationRequestDTO",
                    requestDTO
            );


            return "vendor/quotation-create";
        }
    }


    // =====================================================
    // MY QUOTATIONS
    // =====================================================

    @GetMapping("/list")
    public String myQuotations(

            HttpSession session,

            Model model,

            @RequestParam(
                    value = "submitted",
                    required = false
            )
            Boolean submitted) {


        // -----------------------------------------
        // CHECK LOGIN
        // -----------------------------------------

        Long vendorId =
                (Long) session.getAttribute("userId");

        if (vendorId == null) {
            return "redirect:/auth/login";
        }


        // -----------------------------------------
        // CHECK ROLE
        // -----------------------------------------

        String role =
                String.valueOf(
                        session.getAttribute("userRole")
                );

        if (!"VENDOR".equalsIgnoreCase(role)) {
            return "redirect:/auth/login";
        }


        // -----------------------------------------
        // GET VENDOR QUOTATIONS
        // -----------------------------------------

        List<QuotationResponseDTO> quotations =
                quotationService
                        .getVendorQuotations(vendorId);


        // -----------------------------------------
        // SEND TO THYMELEAF
        // -----------------------------------------

        model.addAttribute(
                "quotations",
                quotations
        );


        model.addAttribute(
                "submitted",
                submitted
        );


        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );


        return "vendor/quotation-list";
    }


    // =====================================================
    // VIEW MY QUOTATION
    // =====================================================

    @GetMapping("/view/{id}")
    public String viewQuotation(

            @PathVariable Long id,

            HttpSession session,

            Model model) {


        // -----------------------------------------
        // CHECK LOGIN
        // -----------------------------------------

        Long vendorId =
                (Long) session.getAttribute("userId");

        if (vendorId == null) {
            return "redirect:/auth/login";
        }


        // -----------------------------------------
        // CHECK ROLE
        // -----------------------------------------

        String role =
                String.valueOf(
                        session.getAttribute("userRole")
                );

        if (!"VENDOR".equalsIgnoreCase(role)) {
            return "redirect:/auth/login";
        }


        try {

            // -----------------------------------------
            // GET QUOTATION
            // -----------------------------------------

            Quotation quotation =
                    quotationService
                            .getQuotationForVendor(
                                    id,
                                    vendorId
                            );


            // -----------------------------------------
            // SEND QUOTATION TO THYMELEAF
            // -----------------------------------------

            model.addAttribute(
                    "quotation",
                    quotation
            );


            return "vendor/quotation-view";


        } catch (Exception e) {

            e.printStackTrace();

            return "redirect:/vendor/quotation/list";
        }
    }
}