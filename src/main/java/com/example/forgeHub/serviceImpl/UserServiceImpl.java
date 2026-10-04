package com.example.forgeHub.serviceImpl;

import com.example.forgeHub.dto.request.UserRequestDTO;
import com.example.forgeHub.dto.response.UserResponseDTO;
import com.example.forgeHub.exception.DuplicateResourceException;
import com.example.forgeHub.model.User;
import com.example.forgeHub.repository.UserRepository;
import com.example.forgeHub.mapper.UserMapper;
import com.example.forgeHub.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private UserMapper userMapper;

    //Register User
    public UserResponseDTO registerUser(UserRequestDTO userRequestDTO)
    {

        if (userRepository.existsByEmail(
                userRequestDTO.getEmail())) {

            throw new DuplicateResourceException(
                    "Email already exists : "
                            + userRequestDTO.getEmail()
            );
        }


        if (userRequestDTO.getMobileNo() != null
                && !userRequestDTO.getMobileNo().isBlank()
                && userRepository.existsByMobileNo(
                userRequestDTO.getMobileNo())) {

            throw new DuplicateResourceException(
                    "Phone number already exists : "
                            + userRequestDTO.getMobileNo()
            );
        }
        User user=User.builder()
                .fullName(userRequestDTO.getFullName())
                .email(userRequestDTO.getEmail())
                .role(userRequestDTO.getRole())
                .address(userRequestDTO.getAddress())
                .password(userRequestDTO.getPassword())
                .companyName(userRequestDTO.getCompanyName())
                .gstNo(userRequestDTO.getGstNo())
                .mobileNo(userRequestDTO.getMobileNo())
                .build();

        User saveUser=userRepository.save(user);

        return userMapper.toResponse(saveUser);
    }


}
