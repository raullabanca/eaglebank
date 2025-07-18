package com.eaglebank.mapper;

import com.eaglebank.dto.CreateUserRequestDto;
import com.eaglebank.dto.UserResponseDto;
import com.eaglebank.model.User;

public class UserMapper {

    public static User toEntity(final CreateUserRequestDto dto) {
        return new User(dto.name(), dto.email(), dto.password(), dto.phoneNumber(),
                AddressMapper.toEntity(dto.address()));
    }

    public static UserResponseDto toDto(final User user) {
        return new UserResponseDto(user.getId(), user.getName(),
                AddressMapper.toDto(user.getAddress()),
                user.getPhoneNumber(), user.getEmail(), user.getCreatedTimestamp(),
                user.getUpdatedTimestamp());
    }
}
