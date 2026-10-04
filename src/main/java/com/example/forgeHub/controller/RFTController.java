package com.example.forgeHub.controller;

import com.example.forgeHub.dto.request.RFTRequestDTO;
import com.example.forgeHub.dto.response.RFTResponseDTO;
import com.example.forgeHub.model.User;
import com.example.forgeHub.repository.UserRepository;
import com.example.forgeHub.serviceImpl.RFTServiceImpl;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/rft")
@AllArgsConstructor
public class RFTController {

    private final RFTServiceImpl rftService;

    private final UserRepository userRepository;


    // =====================================================
    // CREATE PAGE
    // =====================================================

    @GetMapping("/create")
    public String showCreateRFTPage(
            Model model,
            HttpSession session) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute(
                "rftRequestDTO",
                new RFTRequestDTO()
        );

        List<User> vendors =
                userRepository.findByRole("VENDOR");

        model.addAttribute(
                "vendors",
                vendors
        );

        return "admin/rft-create";
    }


    // =====================================================
    // CREATE RFT
    // =====================================================

    @PostMapping("/create")
    public String createRFT(

            @ModelAttribute("rftRequestDTO")
            RFTRequestDTO requestDTO,

            @RequestParam(
                    value = "vendorIds",
                    required = false
            )
            List<Long> vendorIds,

            HttpSession session,

            Model model) {

        try {

            Long createdBy =
                    (Long) session.getAttribute("userId");

            if (createdBy == null) {
                return "redirect:/auth/login";
            }

            rftService.createRFT(
                    requestDTO,
                    createdBy,
                    vendorIds
            );

            // CREATE SUCCESS
            return "redirect:/rft/list";

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Failed to create RFT: "
                            + e.getMessage()
            );

            model.addAttribute(
                    "vendors",
                    userRepository.findByRole("VENDOR")
            );

            return "admin/rft-create";
        }
    }


    // =====================================================
    // RFT LIST
    // =====================================================

    @GetMapping("/list")
    public String getRFTList(
            Model model,
            HttpSession session) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/auth/login";
        }

        List<RFTResponseDTO> rfts =
                rftService.getAllRFTs();

        model.addAttribute(
                "rfts",
                rfts
        );

        return "admin/rft-list";
    }


    // =====================================================
    // VIEW RFT
    // =====================================================

    @GetMapping("/view/{id}")
    public String viewRFT(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/auth/login";
        }

        RFTResponseDTO rft =
                rftService.getRFTById(id);

        model.addAttribute(
                "rft",
                rft
        );

        return "admin/rft-view";
    }


    // =====================================================
    // EDIT PAGE
    // =====================================================

    @GetMapping("/edit/{id}")
    public String editRFTPage(
            @PathVariable Long id,
            Model model,
            HttpSession session) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/auth/login";
        }

        RFTResponseDTO rft =
                rftService.getRFTById(id);

        model.addAttribute(
                "rft",
                rft
        );

        return "admin/rft-edit";
    }


    // =====================================================
    // UPDATE RFT
    // =====================================================

    @PostMapping("/update/{id}")
    public String updateRFT(

            @PathVariable Long id,

            @ModelAttribute RFTRequestDTO requestDTO,

            HttpSession session,

            Model model) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/auth/login";
        }

        try {

            rftService.updateRFT(
                    id,
                    requestDTO
            );

            return "redirect:/rft/list";

        } catch (Exception e) {

            // If update fails, load existing RFT again
            RFTResponseDTO rft =
                    rftService.getRFTById(id);

            model.addAttribute(
                    "rft",
                    rft
            );

            model.addAttribute(
                    "errorMessage",
                    "Failed to update RFT: "
                            + e.getMessage()
            );

            return "admin/rft-edit";
        }
    }


    // =====================================================
    // DELETE RFT
    // =====================================================

    @GetMapping("/delete/{id}")
    public String deleteRFT(
            @PathVariable Long id,
            HttpSession session) {

        Long userId =
                (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/auth/login";
        }

        rftService.deleteRFT(id);

        return "redirect:/rft/list";
    }
}