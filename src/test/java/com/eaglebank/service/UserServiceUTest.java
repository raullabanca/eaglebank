package com.eaglebank.service;

import static com.eaglebank.testutils.UserTestCommons.ADDRESS;
import static com.eaglebank.testutils.UserTestCommons.ADDRESS_DTO;
import static com.eaglebank.testutils.UserTestCommons.EMAIL;
import static com.eaglebank.testutils.UserTestCommons.ENCRYPTED_PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.ID;
import static com.eaglebank.testutils.UserTestCommons.NAME;
import static com.eaglebank.testutils.UserTestCommons.PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.PHONE_NUMBER;
import static com.eaglebank.testutils.UserTestCommons.buildUpdateUserRequestDto;
import static com.eaglebank.testutils.UserTestCommons.buildUser;
import static com.eaglebank.testutils.UserTestCommons.buildUserRequestDto;
import static com.eaglebank.testutils.UserTestCommons.buildUserResponseDto;
import static com.eaglebank.testutils.UserTestCommons.buildUserWithId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eaglebank.dto.CreateUserRequestDto;
import com.eaglebank.dto.UpdateUserRequestDto;
import com.eaglebank.dto.UserResponseDto;
import com.eaglebank.mapper.UserMapper;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService testObj;


    @Test
    void createUser_allCorrectParams_shouldReturnUserCreated() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCRYPTED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(buildUserWithId());

        final UserResponseDto actual = testObj.createUser(buildUserRequestDto());

        assertNotNull(actual);
        assertEquals(ID, actual.id());
        assertEquals(NAME, actual.name());
        assertEquals(EMAIL, actual.email());
        assertEquals(ADDRESS_DTO, actual.address());
        assertEquals(PHONE_NUMBER, actual.phoneNumber());
    }

    @Test
    void createUser_existingEmail_shouldThrowException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(buildUser()));

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testObj.createUser(buildUserRequestDto()),
                "Email is already in use"
        );

        assertTrue(exception.getMessage()
                .contains("Email is already in use"));
    }

    @Test
    void createUser_somethingGoesWrongWhenSaving_shouldThrowException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCRYPTED_PASSWORD);
        final String message = "Something went wrong";
        doThrow(new RuntimeException(message)).when(userRepository)
                .save(any(User.class));

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.createUser(buildUserRequestDto()),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void createUser_allCorrectParams_shouldEncryptPassword() {
        final CreateUserRequestDto requestDto = buildUserRequestDto();
        final UserResponseDto userResponseDto = buildUserResponseDto(ID);

        final User userEntity = buildUser();

        try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {
            mapperMock.when(() -> UserMapper.toEntity(requestDto))
                    .thenReturn(userEntity);
            mapperMock.when(() -> UserMapper.toDto(any(User.class)))
                    .thenReturn(userResponseDto);

            when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
            when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCRYPTED_PASSWORD);

            final ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenReturn(userEntity);

            testObj.createUser(requestDto);

            final User savedUser = userCaptor.getValue();
            assertEquals(ENCRYPTED_PASSWORD, savedUser.getPassword());
        }
    }

    @Test
    void getUser_withCorrectId_shouldReturnUserDetails() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(buildUserWithId()));

        final UserResponseDto actual = testObj.getUser(ID);

        assertNotNull(actual);

        assertEquals(ID, actual.id());
        assertEquals(EMAIL, actual.email());
        assertEquals(NAME, actual.name());
        assertEquals(ADDRESS_DTO, actual.address());
        assertEquals(PHONE_NUMBER, actual.phoneNumber());
    }

    @Test
    void getUser_withNonExistingId_shouldThrowException() {
        final String message = "User not found";
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.getUser(ID),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void getUser_somethingGoesWrong_shouldThrowException() {
        final String message = "Something went wrong";
        doThrow(new RuntimeException(message)).when(userRepository)
                .findById(ID);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.getUser(ID),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void updateUser_withCorrectId_shouldReturnUserDetails() {
        final String email = "mario@email.com";
        final String password = "password321";
        final String name = "Mario";
        final String encodedPass = "encodedPass";
        final User expected = new User(name, email, password, PHONE_NUMBER, ADDRESS);
        expected.setId(ID);

        when(userRepository.findById(ID)).thenReturn(Optional.of(buildUserWithId()));
        when(passwordEncoder.encode(password)).thenReturn(encodedPass);
        when(userRepository.save(any(User.class))).thenReturn(expected);

        final UserResponseDto actual = testObj.updateUser(ID,
                new UpdateUserRequestDto(name, email, ADDRESS_DTO,
                        PHONE_NUMBER, password));

        assertNotNull(actual);
        assertEquals(ID, actual.id());
        assertEquals(email, actual.email());
        assertEquals(name, actual.name());
        assertEquals(ADDRESS_DTO, actual.address());
        assertEquals(PHONE_NUMBER, actual.phoneNumber());
    }

    @Test
    void updateUser_withNonExistingId_shouldThrowException() {
        final String message = "User not found";
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.updateUser(ID, buildUpdateUserRequestDto()),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void updateUser_somethingGoesWrongWhenSaving_shouldThrowException() {
        when(userRepository.findById(ID)).thenReturn(Optional.of(buildUserWithId()));
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCRYPTED_PASSWORD);
        final String message = "Something went wrong";
        doThrow(new RuntimeException(message)).when(userRepository)
                .save(any(User.class));

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.updateUser(ID, buildUpdateUserRequestDto()),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void deleteUser_withCorrectId_shouldDelete() {
        final User user = buildUserWithId();

        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        doNothing().when(userRepository)
                .delete(user);

        testObj.deleteUser(ID);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_somethingGoesWrongWhenDeleting_shouldThrowException() {
        final User user = buildUserWithId();
        final String message = "Something went wrong";

        when(userRepository.findById(ID)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException(message)).when(userRepository)
                .delete(user);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.deleteUser(ID),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void deleteUser_withNonExistingId_shouldThrowException() {
        final String message = "User not found";
        when(userRepository.findById(ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.deleteUser(ID),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }
}