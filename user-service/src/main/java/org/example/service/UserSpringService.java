package org.example.service;

import org.example.dto.UserRequestDto;
import org.example.dto.UserResponseDto;

import java.util.List;

public interface UserSpringService {

    UserResponseDto createUser(UserRequestDto request);
    UserResponseDto getUserById(Long id);
    UserResponseDto getUserByEmail(String email);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(Long id, UserRequestDto request);
    void deleteUser(Long id);
}
