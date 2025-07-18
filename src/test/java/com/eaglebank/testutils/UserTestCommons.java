package com.eaglebank.testutils;

import com.eaglebank.dto.AddressDto;
import com.eaglebank.dto.CreateUserRequestDto;
import com.eaglebank.dto.UpdateUserRequestDto;
import com.eaglebank.dto.UserResponseDto;
import com.eaglebank.model.User;
import com.eaglebank.security.AuthenticatedUser;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class UserTestCommons {

    public static final String NAME = "John";
    public static final String EMAIL = "john@email.com";
    public static final String PASSWORD = "password123";
    public static final String ENCRYPTED_PASSWORD = "encrypted-password123";
    public static final String ID = "1L";
    public static final String LINE_1 = "line1";
    public static final String LINE_2 = "line2";
    public static final String LINE_3 = "line3";
    public static final String TOWN = "town";
    public static final String COUNTY = "county";
    public static final String POSTCODE = "postcode";
    public static final AddressDto ADDRESS_DTO = new AddressDto(LINE_1, LINE_2, LINE_3, TOWN,
            COUNTY,
            POSTCODE);
    public static final com.eaglebank.model.Address ADDRESS = new com.eaglebank.model.Address(
            LINE_1, LINE_2, LINE_3, TOWN, COUNTY,
            POSTCODE);
    public static final String PHONE_NUMBER = "+447911123456";
    public static final OffsetDateTime NOW = OffsetDateTime.now();

    public static String generateUserId() {
        String randomAlphanumeric = UUID.randomUUID()
                .toString()
                .replaceAll("-", "")
                .substring(0, 12); // Or any desired length
        return "usr-" + randomAlphanumeric;
    }

    public static User buildUser() {
        return new User(NAME, EMAIL, PASSWORD, PHONE_NUMBER, ADDRESS);
    }

    public static User buildUserWithId() {
        final User user = new User(NAME, EMAIL, PASSWORD, PHONE_NUMBER, ADDRESS);
        user.setId(ID);
        return user;
    }

    public static User buildUserWithEncryptedPassword() {
        return new User(NAME, EMAIL, ENCRYPTED_PASSWORD, PHONE_NUMBER, ADDRESS);
    }

    public static UserResponseDto buildUserResponseDto(final String id) {
        return new UserResponseDto(id, NAME, ADDRESS_DTO, PHONE_NUMBER, EMAIL, NOW, NOW);
    }

    public static CreateUserRequestDto buildUserRequestDto() {
        return new CreateUserRequestDto(NAME, ADDRESS_DTO, PHONE_NUMBER, EMAIL, PASSWORD);
    }

    public static CreateUserRequestDto buildUserRequestDto(String name,
            String email,
            String password) {
        return new CreateUserRequestDto(name, ADDRESS_DTO, PHONE_NUMBER, email, password);
    }

    public static UpdateUserRequestDto buildUpdateUserRequestDto() {
        return new UpdateUserRequestDto(NAME, EMAIL, ADDRESS_DTO, PHONE_NUMBER, PASSWORD);
    }

    public static AuthenticatedUser buildAuthenticatedUser(final String id) {
        return new AuthenticatedUser(id, EMAIL, List.of());
    }
}
