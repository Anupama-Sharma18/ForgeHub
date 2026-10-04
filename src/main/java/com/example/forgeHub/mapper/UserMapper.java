package com.example.forgeHub.mapper;

import com.example.forgeHub.dto.response.UserResponseDTO;
import com.example.forgeHub.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponse(User user) {

        return UserResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .companyName(user.getCompanyName())
                .address(user.getAddress())
                .mobileNo(user.getMobileNo())
                .email(user.getEmail())
                .role(user.getRole())
                .gstNo(user.getGstNo())
                .isFirstTimeLogin(user.getIsFirstTimeLogin())
                .build();
    }
}