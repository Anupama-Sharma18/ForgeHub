package com.example.forgeHub.controller;

import com.example.forgeHub.dto.response.QuotationResponseDTO;
import com.example.forgeHub.serviceImpl.QuotationServiceImpl;

import lombok.AllArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminController {

    private final QuotationServiceImpl quotationService;


    // =====================================================
    // ADMIN DASHBOARD
    // =====================================================

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model
    ) {

        // -------------------------------------------------
        // Check authentication
        // -------------------------------------------------

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return "redirect:/login";
        }

        // -------------------------------------------------
        // Get role from JWT authentication
        // -------------------------------------------------

        String role = authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        // -------------------------------------------------
        // Check ADMIN role
        // -------------------------------------------------

        if (!"ROLE_ADMIN".equalsIgnoreCase(role)
                && !"ADMIN".equalsIgnoreCase(role)) {

            return "redirect:/login";
        }

        // -------------------------------------------------
        // Send user data to Thymeleaf
        // -------------------------------------------------

        model.addAttribute(
                "userName",
                authentication.getName()
        );

        model.addAttribute(
                "userRole",
                "ADMIN"
        );

        return "admin/dashboard";
    }


    // =====================================================
    // QUOTATION LIST
    // =====================================================

    @GetMapping("/quotations")
    public String quotationList(
            Authentication authentication,
            Model model,
            @RequestParam(
                    value = "approved",
                    required = false
            )
            Boolean approved
    ) {

        // -------------------------------------------------
        // Check login + admin
        // -------------------------------------------------

        if (!isAdmin(authentication)) {
            return "redirect:/login";
        }

        // -------------------------------------------------
        // Get quotations
        // -------------------------------------------------

        List<QuotationResponseDTO> quotations =
                quotationService.getAllQuotations();

        model.addAttribute(
                "quotations",
                quotations
        );

        model.addAttribute(
                "approved",
                approved
        );

        model.addAttribute(
                "userName",
                authentication.getName()
        );

        model.addAttribute(
                "userRole",
                "ADMIN"
        );

        return "admin/quotation-list";
    }


    // =====================================================
    // APPROVE QUOTATION
    // =====================================================

    @PostMapping("/quotations/{id}/approve")
    public String approveQuotation(
            @PathVariable Long id,
            Authentication authentication
    ) {

        // -------------------------------------------------
        // Check login + admin
        // -------------------------------------------------

        if (!isAdmin(authentication)) {
            return "redirect:/login";
        }

        // -------------------------------------------------
        // Approve quotation
        // -------------------------------------------------

        quotationService.approveQuotation(id);

        return "redirect:/admin/quotations?approved=true";
    }


    // =====================================================
    // REJECT QUOTATION
    // =====================================================

    @PostMapping("/quotations/{id}/reject")
    public String rejectQuotation(
            @PathVariable Long id,
            Authentication authentication
    ) {

        // -------------------------------------------------
        // Check login + admin
        // -------------------------------------------------

        if (!isAdmin(authentication)) {
            return "redirect:/login";
        }

        // -------------------------------------------------
        // Reject quotation
        // -------------------------------------------------

        quotationService.rejectQuotation(id);

        return "redirect:/admin/quotations?rejected=true";
    }


    // =====================================================
    // COMMON ADMIN CHECK
    // =====================================================

    private boolean isAdmin(Authentication authentication) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equalsIgnoreCase(
                                authority.getAuthority()
                        )
                                ||
                                "ADMIN".equalsIgnoreCase(
                                        authority.getAuthority()
                                )
                );
    }
}
