package com.example.forgeHub.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestController {


    // =========================================================
    // COMMON AUTHENTICATED API
    // =========================================================

    @GetMapping("/test/hello")
    public Map<String, Object> hello(
            Authentication authentication
    ) {

        return Map.of(
                "message",
                "JWT authentication successful",

                "username",
                authentication.getName(),

                "authorities",
                authentication.getAuthorities()
        );
    }


    // =========================================================
    // ADMIN API
    // =========================================================

    @GetMapping("/admin/dashboard")
    public Map<String, Object> adminDashboard(
            Authentication authentication
    ) {

        return Map.of(
                "message",
                "Welcome Admin",

                "username",
                authentication.getName(),

                "role",
                "ADMIN"
        );
    }


    // =========================================================
    // VENDOR API
    // =========================================================

    @GetMapping("/vendor/dashboard")
    public Map<String, Object> vendorDashboard(
            Authentication authentication
    ) {

        return Map.of(
                "message",
                "Welcome Vendor",

                "username",
                authentication.getName(),

                "role",
                "VENDOR"
        );
    }
}