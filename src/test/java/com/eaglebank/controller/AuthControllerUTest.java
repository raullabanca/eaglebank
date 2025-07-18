package com.eaglebank.controller;

import static com.eaglebank.testutils.UserTestCommons.EMAIL;
import static com.eaglebank.testutils.UserTestCommons.ENCRYPTED_PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.buildUserWithEncryptedPassword;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.eaglebank.dto.AuthRequestDto;
import com.eaglebank.dto.AuthResponseDto;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthControllerUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController testObj;

    @Test
    void login_withValidPassword_shouldReturnToken() {
        final User user = buildUserWithEncryptedPassword();
        final String token = "token";
        when(userRepository.findByEmail(EMAIL)).thenReturn(
                Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, ENCRYPTED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn(token);
        final ResponseEntity<AuthResponseDto> actual = testObj.login(
                new AuthRequestDto(EMAIL, PASSWORD));

        assertNotNull(actual);
        assertEquals(HttpStatusCode.valueOf(200), actual.getStatusCode());
        assertEquals(token, actual.getBody()
                .token());
    }

    @Test
    void login_withNonExistingEmail_shouldThrowException() {
        final String message = "User not found";
        doThrow(new EntityNotFoundException(message)).when(userRepository)
                .findByEmail(EMAIL);

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.login(new AuthRequestDto(EMAIL, PASSWORD)),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void login_withInvalidPassword_shouldThrowException() {
        final User user = buildUserWithEncryptedPassword();
        when(userRepository.findByEmail(EMAIL)).thenReturn(
                Optional.of(user));
        final String message = "Invalid password";
        when(passwordEncoder.matches(PASSWORD, ENCRYPTED_PASSWORD)).thenReturn(false);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.login(new AuthRequestDto(EMAIL, PASSWORD)),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }
}