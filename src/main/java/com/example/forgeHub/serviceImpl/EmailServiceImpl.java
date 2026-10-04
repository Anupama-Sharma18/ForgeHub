package com.example.forgeHub.serviceImpl;

import com.example.forgeHub.model.User;
import com.example.forgeHub.repository.UserRepository;
import com.example.forgeHub.service.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;
    private final PasswordEncoder passwordEncoder;

    // =========================================================
    // OTP MEMORY STORE
    // =========================================================
    // OTP DB mein store nahi hoga.
    // Sirf memory mein hash + expiry store hoga.
    // =========================================================

    private final ConcurrentHashMap<String, OtpData> otpStore =
            new ConcurrentHashMap<>();

    private final SecureRandom secureRandom =
            new SecureRandom();

    // =========================================================
    // SEND OTP
    // =========================================================

    @Override
    public void sendOtp(String email) {

        String normalizedEmail =
                email.trim().toLowerCase();

        log.info(
                "OTP generation requested for user: {}",
                normalizedEmail
        );

        // ---------------------------------------------------------
        // STEP 1: Find user
        // ---------------------------------------------------------

        User user =
                userRepository.findByEmail(normalizedEmail)
                        .orElseThrow(() -> {

                            log.warn(
                                    "OTP request failed - unregistered email: {}",
                                    normalizedEmail
                            );

                            return new RuntimeException(
                                    "Email not registered"
                            );
                        });

        log.debug(
                "User found successfully for OTP request: {}",
                normalizedEmail
        );

        // ---------------------------------------------------------
        // STEP 2: Generate 6 digit OTP
        // ---------------------------------------------------------

        String otp =
                String.format(
                        "%06d",
                        secureRandom.nextInt(1_000_000)
                );

        // IMPORTANT:
        // Raw OTP ko log nahi karna hai.

        log.debug(
                "6-digit OTP generated successfully for user: {}",
                normalizedEmail
        );

        // ---------------------------------------------------------
        // STEP 3: Hash OTP
        // ---------------------------------------------------------

        String otpHash =
                passwordEncoder.encode(otp);

        log.debug(
                "OTP hash generated successfully for user: {}",
                normalizedEmail
        );

        // ---------------------------------------------------------
        // STEP 4: Store OTP in memory
        // ---------------------------------------------------------

        LocalDateTime expiry =
                LocalDateTime.now().plusMinutes(5);

        otpStore.put(
                normalizedEmail,
                new OtpData(
                        otpHash,
                        expiry
                )
        );

        log.info(
                "OTP stored in memory successfully for user: {}. Expiry={}",
                normalizedEmail,
                expiry
        );

        // ---------------------------------------------------------
        // STEP 5: Prepare email
        // ---------------------------------------------------------

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(
                user.getEmail()
        );

        message.setSubject(
                "AIG ForgeHub - OTP Verification"
        );

        message.setText(
                "Hello " + user.getFullName() + ",\n\n"
                        + "Your AIG ForgeHub OTP is: " + otp + "\n\n"
                        + "This OTP is valid for 5 minutes.\n\n"
                        + "If you did not request this OTP, please ignore this email."
        );

        // ---------------------------------------------------------
        // STEP 6: Send email
        // ---------------------------------------------------------

        try {

            javaMailSender.send(message);

            log.info(
                    "OTP email sent successfully to user: {}",
                    normalizedEmail
            );

        } catch (Exception e) {

            // Email failed, so stored OTP remove kar do.
            otpStore.remove(normalizedEmail);

            log.error(
                    "Failed to send OTP email to user: {}",
                    normalizedEmail,
                    e
            );

            throw new RuntimeException(
                    "Unable to send OTP email",
                    e
            );
        }
    }

    // =========================================================
    // VERIFY OTP
    // =========================================================

    @Override
    public boolean verifyOtp(
            String email,
            String otp
    ) {

        String normalizedEmail =
                email.trim().toLowerCase();

        log.info(
                "OTP verification requested for user: {}",
                normalizedEmail
        );

        // ---------------------------------------------------------
        // STEP 1: Get OTP from memory
        // ---------------------------------------------------------

        OtpData otpData =
                otpStore.get(normalizedEmail);

        if (otpData == null) {

            log.warn(
                    "OTP verification failed - no OTP found for user: {}",
                    normalizedEmail
            );

            return false;
        }

        log.debug(
                "OTP record found in memory for user: {}",
                normalizedEmail
        );

        // ---------------------------------------------------------
        // STEP 2: Check expiry
        // ---------------------------------------------------------

        LocalDateTime now =
                LocalDateTime.now();

        if (otpData.expiry().isBefore(now)) {

            otpStore.remove(normalizedEmail);

            log.warn(
                    "OTP verification failed - OTP expired for user: {}",
                    normalizedEmail
            );

            log.debug(
                    "Expired OTP removed from memory for user: {}",
                    normalizedEmail
            );

            return false;
        }

        // ---------------------------------------------------------
        // STEP 3: Validate OTP
        // ---------------------------------------------------------

        boolean valid =
                passwordEncoder.matches(
                        otp,
                        otpData.otpHash()
                );

        if (!valid) {

            log.warn(
                    "OTP verification failed - invalid OTP for user: {}",
                    normalizedEmail
            );

            return false;
        }

        // ---------------------------------------------------------
        // STEP 4: OTP verified successfully
        // ---------------------------------------------------------

        otpStore.remove(normalizedEmail);

        log.info(
                "OTP verified successfully for user: {}",
                normalizedEmail
        );

        log.debug(
                "Verified OTP removed from memory for user: {}",
                normalizedEmail
        );

        return true;
    }

    // =========================================================
    // TEMPORARY OTP RECORD
    // =========================================================
    // Separate DB model/entity nahi hai.
    // Sirf memory mein temporary data store karne ke liye.
    // =========================================================

    private record OtpData(
            String otpHash,
            LocalDateTime expiry
    ) {
    }
}