package com.example.forgeHub.config;

import com.example.forgeHub.model.User;
import com.example.forgeHub.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Bean
    CommandLineRunner initUsers() {

        return args -> {

            log.info("=================================================");
            log.info("Starting test user initialization");
            log.info("=================================================");


            // =================================================
            // ADMIN USER
            // =================================================

            if (userRepository
                    .findByEmail("ms.anu.sha182003@gmail.com")
                    .isEmpty()) {

                User admin = new User();

                admin.setFullName(
                        "ForgeHub Admin"
                );

                admin.setEmail(
                        "ms.anu.sha182003@gmail.com"
                );

                admin.setPasswordHash(
                        passwordEncoder.encode(
                                "Admin@123"
                        )
                );

                admin.setRole(
                        "ADMIN"
                );

                // IMPORTANT
                // First-time login testing
                admin.setIsFirstTimeLogin(
                        true
                );

                // 2FA abhi configured nahi hai
                admin.setSecretKey(
                        null
                );

                // Refresh token data initially empty
                admin.setRefreshTokenHash(
                        null
                );

                admin.setRefreshJti(
                        null
                );

                admin.setRevoked(
                        false
                );

                userRepository.save(admin);

                log.info(
                        "Admin test user created: {}",
                        admin.getEmail()
                );

            } else {

                log.info(
                        "Admin user already exists: {}",
                        "ms.anu.sha182003@gmail.com"
                );
            }


            // =================================================
            // VENDOR USER
            // =================================================

            if (userRepository
                    .findByEmail("vendor@forgehub.com")
                    .isEmpty()) {

                User vendor = new User();

                vendor.setFullName(
                        "ForgeHub Vendor"
                );

                vendor.setEmail(
                        "vendor@forgehub.com"
                );

                vendor.setPasswordHash(
                        passwordEncoder.encode(
                                "Vendor@123"
                        )
                );

                vendor.setRole(
                        "VENDOR"
                );

                // Vendor bhi first-time login karega
                vendor.setIsFirstTimeLogin(
                        true
                );

                vendor.setSecretKey(
                        null
                );

                vendor.setRefreshTokenHash(
                        null
                );

                vendor.setRefreshJti(
                        null
                );

                vendor.setRevoked(
                        false
                );

                userRepository.save(vendor);

                log.info(
                        "Vendor test user created: {}",
                        vendor.getEmail()
                );

            } else {

                log.info(
                        "Vendor user already exists: {}",
                        "vendor@forgehub.com"
                );
            }


            log.info("=================================================");
            log.info("Test user initialization completed");
            log.info("=================================================");
        };
    }
}

//Email    : admin@forgehub.com
//Password : Admin@123
//Role     : ADMIN
//First Login : true

//Email    : vendor@forgehub.com
//Password : Vendor@123
//Role     : VENDOR
//First Login : true