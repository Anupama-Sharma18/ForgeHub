package com.example.forgeHub.service;

public interface EmailService {

    void sendOtp(String email);

    boolean verifyOtp(String email, String otp);
}