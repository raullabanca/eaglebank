package com.eaglebank.service;

import com.eaglebank.dto.CreateUserRequestDto;
import com.eaglebank.dto.UpdateUserRequestDto;
import com.eaglebank.dto.UserResponseDto;
import com.eaglebank.mapper.UserMapper;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDto createUser(final CreateUserRequestDto userRequest) {
        userRepository.findByEmail(userRequest.email())
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("Email is already in use");
                });

        final User user = UserMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedTimestamp(OffsetDateTime.now());
        user.setUpdatedTimestamp(OffsetDateTime.now());

        return UserMapper.toDto(userRepository.save(user));
    }

    public UserResponseDto getUser(final String userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return UserMapper.toDto(user);
    }

    public UserResponseDto updateUser(final String userId, final UpdateUserRequestDto userRequest) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (userRequest.name() != null) {
            user.setName(userRequest.name());
        }
        if (userRequest.email() != null) {
            user.setEmail(userRequest.email());
        }
        if (userRequest.password() != null) {
            user.setPassword(passwordEncoder.encode(userRequest.password()));
        }

        user.setUpdatedTimestamp(OffsetDateTime.now());

        return UserMapper.toDto(userRepository.save(user));
    }

    public void deleteUser(final String userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        userRepository.delete(user);
    }
}
