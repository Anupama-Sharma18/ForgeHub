package com.example.forgeHub.repository;

import com.example.forgeHub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(
            String email
    );

    boolean existsByMobileNo(
            String phone
    );

    Optional<User> findByEmail(String email);

    List<User> findByRole(String role);

    Optional<User> findByRefreshJti(String refreshJti);
}
