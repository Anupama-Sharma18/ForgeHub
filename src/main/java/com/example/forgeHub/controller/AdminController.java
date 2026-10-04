package com.example.forgeHub.controller;

import com.example.forgeHub.dto.response.QuotationResponseDTO;
import com.example.forgeHub.serviceImpl.QuotationServiceImpl;

import jakarta.servlet.http.HttpSession;

import lombok.AllArgsConstructor;

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
            HttpSession session,
            Model model) {


        Long userId =
                (Long) session.getAttribute(
                        "userId"
                );


        if (userId == null) {

            return "redirect:/auth/login";
        }


        String role =
                (String) session.getAttribute(
                        "userRole"
                );


        if (!"ADMIN".equalsIgnoreCase(role)) {

            return "redirect:/auth/login";
        }


        model.addAttribute(
                "userName",
                session.getAttribute("userName")
        );


        model.addAttribute(
                "userRole",
                role
        );


        return "admin/dashboard";
    }


    // =====================================================
    // QUOTATION LIST
    // =====================================================

    @GetMapping("/quotations")
    public String quotationList(

            HttpSession session,

            Model model,

            @RequestParam(
                    value = "approved",
                    required = false
            )
            Boolean approved) {


        Long adminId =
                (Long) session.getAttribute(
                        "userId"
                );


        if (adminId == null) {

            return "redirect:/auth/login";
        }


        String role =
                (String) session.getAttribute(
                        "userRole"
                );


        if (!"ADMIN".equalsIgnoreCase(role)) {

            return "redirect:/auth/login";
        }


        List<QuotationResponseDTO> quotations =
                quotationService
                        .getAllQuotations();


        model.addAttribute(
                "quotations",
                quotations
        );


        model.addAttribute(
                "approved",
                approved
        );


        return "admin/quotation-list";
    }


    // =====================================================
    // APPROVE
    // =====================================================

    @PostMapping("/quotations/{id}/approve")
    public String approveQuotation(

            @PathVariable Long id,

            HttpSession session) {


        Long adminId =
                (Long) session.getAttribute(
                        "userId"
                );


        if (adminId == null) {

            return "redirect:/auth/login";
        }


        String role =
                (String) session.getAttribute(
                        "userRole"
                );


        if (!"ADMIN".equalsIgnoreCase(role)) {

            return "redirect:/auth/login";
        }


        quotationService.approveQuotation(id);


        return "redirect:/admin/quotations?approved=true";
    }


    // =====================================================
    // REJECT
    // =====================================================

    @PostMapping("/quotations/{id}/reject")
    public String rejectQuotation(

            @PathVariable Long id,

            HttpSession session) {


        Long adminId =
                (Long) session.getAttribute(
                        "userId"
                );


        if (adminId == null) {

            return "redirect:/auth/login";
        }


        String role =
                (String) session.getAttribute(
                        "userRole"
                );


        if (!"ADMIN".equalsIgnoreCase(role)) {

            return "redirect:/auth/login";
        }


        quotationService.rejectQuotation(id);


        return "redirect:/admin/quotations?rejected=true";
    }
}