package com.example.forgeHub.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthViewController {

    @GetMapping({"/", "/login"})
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/2fa")
    public String twoFactorPage() {
        return "auth/2fa";
    }

    @GetMapping("/2fa/setup")
    public String setup2faPage() {
        return "auth/setup-2fa";
    }

    @GetMapping("/2fa/recovery/email")
    public String recoveryEmailPage() {
        return "auth/recovery-email";
    }

    @GetMapping("/2fa/recovery/otp")
    public String recoveryOtpPage() {
        return "auth/recovery-otp";
    }

    @GetMapping("/2fa/recovery/setup")
    public String recoverySetupPage() {
        return "auth/recovery-2fa";
    }


//    @GetMapping("/admin/dashboard")
//    public String adminDashboardPage() {
//        return "admin/dashboard";
//    }
//
//    @GetMapping("/vendor/dashboard")
//    public String vendorDashboardPage() {
//        return "vendor/dashboard";
//    }
}