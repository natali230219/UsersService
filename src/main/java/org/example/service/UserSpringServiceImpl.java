package org.example.service;

import jakarta.transaction.Transactional;
import org.example.dto.UserRequestDto;
import org.example.dto.UserResponseDto;
import org.example.entities.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserSpringServiceImpl implements UserSpringService {
    private final UserRepository userRepository;

    public UserSpringServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDto createUser(UserRequestDto request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с email " + request.getEmail() + " уже существует");
        }
        User user = new User(request.getName(), request.getEmail(), request.getAge());
        User save = userRepository.save(user);
        return toResponseDto(save);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(()->
                new RuntimeException("Пользователь с ID " + id + " не найден"));
        return toResponseDto(user);
    }

    @Override
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Пользователь с email " + " не найден"));
        return toResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream().
                map(this::toResponseDto).
                collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto request) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Пользователь с ID " + id + " не найден"));
        if (!user.getEmail().equals(request.getEmail()) && !userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с email " + request.getEmail() + " уже существует");
        }
        user.setUserName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        User save = userRepository.save(user);
        return toResponseDto(save);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
    }

    private UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedTime()
        );
    }
}
