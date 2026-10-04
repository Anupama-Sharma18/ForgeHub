package com.example.forgeHub.controller;

import com.example.forgeHub.model.RFTVendor;
import com.example.forgeHub.repository.RFTVendorRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/vendor")
@AllArgsConstructor
public class VendorRFTController {

    private final RFTVendorRepository rftVendorRepository;


    // =========================================
    // VENDOR DASHBOARD
    // =========================================

    @GetMapping("/dashboard")
    public String vendorDashboard(
            HttpSession session,
            Model model) {

        Long vendorId =
                (Long) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("userRole");


        if (vendorId == null) {
            return "redirect:/auth/login";
        }


        if (!"VENDOR".equalsIgnoreCase(role)) {
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


        return "vendor/dashboard";
    }


    // =========================================
    // ASSIGNED RFT LIST
    // =========================================

    @GetMapping("/rfts")
    public String assignedRFTs(
            HttpSession session,
            Model model) {

        Long vendorId =
                (Long) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("userRole");


        if (vendorId == null) {
            return "redirect:/auth/login";
        }


        if (!"VENDOR".equalsIgnoreCase(role)) {
            return "redirect:/auth/login";
        }


        List<RFTVendor> assignments =
                rftVendorRepository
                        .findByVendor_Id(vendorId);


        model.addAttribute(
                "assignments",
                assignments
        );


        return "vendor/rft-list";
    }


    // =========================================
    // OPEN ASSIGNED RFT
    // =========================================

    @GetMapping("/rft/{id}")
    public String openRFT(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Long vendorId =
                (Long) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("userRole");


        if (vendorId == null) {
            return "redirect:/auth/login";
        }


        if (!"VENDOR".equalsIgnoreCase(role)) {
            return "redirect:/auth/login";
        }


        RFTVendor assignment =
                rftVendorRepository
                        .findByVendor_Id(vendorId)
                        .stream()
                        .filter(x ->
                                x.getRft()
                                        .getRftId()
                                        .equals(id)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "RFT is not assigned to this vendor"
                                )
                        );


        model.addAttribute(
                "rft",
                assignment.getRft()
        );


        return "vendor/rft-details";
    }
}