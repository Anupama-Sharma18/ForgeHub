package com.example.forgeHub.controller;

import com.example.forgeHub.dto.request.UserRequestDTO;
import com.example.forgeHub.dto.response.UserResponseDTO;
import com.example.forgeHub.serviceImpl.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private final UserServiceImpl userService;

    // Open registration page
    @GetMapping("/register")
    public String showRegisterPage(Model model) {

        model.addAttribute("userRequestDTO", new UserRequestDTO());

        return "admin/register";
    }

    // Submit registration form
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("userRequestDTO")
            UserRequestDTO userRequestDTO,
            Model model) {

        try {

            UserResponseDTO user =
                    userService.registerUser(userRequestDTO);

            model.addAttribute(
                    "successMessage",
                    "User Registered Successfully"
            );

            model.addAttribute("user", user);

            // Clear form
            model.addAttribute(
                    "userRequestDTO",
                    new UserRequestDTO()
            );

            return "admin/register";

        } catch (Exception e) {

            model.addAttribute(
                    "errorMessage",
                    "Registration failed: " + e.getMessage()
            );

            return "admin/register";
        }
    }
}