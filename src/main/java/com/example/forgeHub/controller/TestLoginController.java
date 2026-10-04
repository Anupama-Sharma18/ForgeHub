package com.example.forgeHub.controller;

import com.example.forgeHub.dto.request.LoginRequestDTO;
import com.example.forgeHub.model.User;
import com.example.forgeHub.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@AllArgsConstructor
public class TestLoginController {

    private final UserRepository userRepository;


    // =========================================
    // LOGIN PAGE
    // =========================================

    @GetMapping("/login")
    public String showLoginPage(Model model) {

        model.addAttribute(
                "loginRequestDTO",
                new LoginRequestDTO()
        );

        return "auth/login";
    }


    // =========================================
    // LOGIN
    // =========================================

    @PostMapping("/login")
    public String login(

            @ModelAttribute("loginRequestDTO")
            LoginRequestDTO loginRequestDTO,

            HttpSession session,

            Model model) {

        User user = userRepository
                .findByEmail(loginRequestDTO.getEmail())
                .orElse(null);


        // User not found
        if (user == null) {

            model.addAttribute(
                    "errorMessage",
                    "Invalid email or password"
            );

            return "auth/login";
        }


        // =========================================
        // PASSWORD CHECK
        // TESTING PURPOSE ONLY
        // =========================================

        if (!user.getPassword()
                .equals(loginRequestDTO.getPassword())) {

            model.addAttribute(
                    "errorMessage",
                    "Invalid email or password"
            );

            return "auth/login";
        }


        // =========================================
        // CREATE SESSION
        // =========================================

        session.setAttribute(
                "userId",
                user.getId()
        );

        session.setAttribute(
                "userName",
                user.getFullName()
        );

        session.setAttribute(
                "userRole",
                user.getRole()
        );


        // =========================================
        // ROLE BASED REDIRECT
        // =========================================

        if ("ADMIN".equalsIgnoreCase(user.getRole())) {

            return "redirect:/admin/dashboard";

        }


        if ("VENDOR".equalsIgnoreCase(user.getRole())) {

            return "redirect:/vendor/dashboard";

        }


        // Unknown role
        session.invalidate();

        model.addAttribute(
                "errorMessage",
                "Invalid user role"
        );

        return "auth/login";
    }


    // =========================================
    // LOGOUT
    // =========================================

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/auth/login";
    }
}