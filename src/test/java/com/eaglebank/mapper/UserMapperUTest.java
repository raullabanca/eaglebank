package com.eaglebank.mapper;

import static com.eaglebank.testutils.UserTestCommons.ID;
import static com.eaglebank.testutils.UserTestCommons.NOW;
import static com.eaglebank.testutils.UserTestCommons.buildUser;
import static com.eaglebank.testutils.UserTestCommons.buildUserRequestDto;
import static com.eaglebank.testutils.UserTestCommons.buildUserResponseDto;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.eaglebank.dto.UserResponseDto;
import com.eaglebank.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserMapperUTest {

    @Test
    void toEntity_user_shouldMapCorrectly() {
        final User expected = buildUser();
        final User actual = UserMapper.toEntity(
                buildUserRequestDto());

        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getPassword(), actual.getPassword());
    }

    @Test
    void toDto_user_shouldMapCorrectly() {
        final User user = buildUser();
        user.setId(ID);
        user.setCreatedTimestamp(NOW);
        user.setUpdatedTimestamp(NOW);
        final UserResponseDto expected = buildUserResponseDto(ID);
        final UserResponseDto actual = UserMapper.toDto(
                user);

        assertEquals(expected.id(), actual.id());
        assertEquals(expected.name(), actual.name());
        assertEquals(expected.email(), actual.email());
        assertEquals(expected.address(), actual.address());
        assertEquals(expected.phoneNumber(), actual.phoneNumber());
        assertEquals(expected.createdTimestamp(), actual.createdTimestamp());
        assertEquals(expected.updatedTimestamp(), actual.updatedTimestamp());
    }
}