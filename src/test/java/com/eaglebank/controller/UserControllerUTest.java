package com.eaglebank.controller;

import static com.eaglebank.testutils.UserTestCommons.ADDRESS_DTO;
import static com.eaglebank.testutils.UserTestCommons.EMAIL;
import static com.eaglebank.testutils.UserTestCommons.ID;
import static com.eaglebank.testutils.UserTestCommons.NAME;
import static com.eaglebank.testutils.UserTestCommons.NOW;
import static com.eaglebank.testutils.UserTestCommons.PASSWORD;
import static com.eaglebank.testutils.UserTestCommons.PHONE_NUMBER;
import static com.eaglebank.testutils.UserTestCommons.buildAuthenticatedUser;
import static com.eaglebank.testutils.UserTestCommons.buildUserRequestDto;
import static com.eaglebank.testutils.UserTestCommons.buildUserResponseDto;
import static com.eaglebank.testutils.UserTestCommons.generateUserId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.eaglebank.dto.CreateUserRequestDto;
import com.eaglebank.dto.UpdateUserRequestDto;
import com.eaglebank.dto.UserResponseDto;
import com.eaglebank.security.AuthenticatedUser;
import com.eaglebank.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class UserControllerUTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController testObj;

    @Test
    void getUser_authenticatedWithCorrectId_shouldReturnDetails() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);
        when(userService.getUser(userId)).thenReturn(buildUserResponseDto(userId));

        final ResponseEntity<UserResponseDto> actual = testObj.getUser(userId, authenticatedUser);

        assertNotNull(actual);

        assertEquals(userId, actual.getBody()
                .id());
        assertEquals(HttpStatusCode.valueOf(200), actual.getStatusCode());
        assertEquals(EMAIL, actual.getBody()
                .email());
        assertEquals(NAME, actual.getBody()
                .name());
        assertEquals(ADDRESS_DTO, actual.getBody()
                .address());
        assertEquals(PHONE_NUMBER, actual.getBody()
                .phoneNumber());
        assertEquals(NOW, actual.getBody()
                .createdTimestamp());
        assertEquals(NOW, actual.getBody()
                .updatedTimestamp());
    }

    @Test
    void getUser_withOtherExistingIdAndAuthenticated_shouldReturnForbidden() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);

        final String message = "You are not allowed to access this resource";
        final AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> testObj.getUser(generateUserId(), authenticatedUser),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void getUser_AuthenticatedWithNonExistingId_shouldNotReturnNotFound() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);

        final String message = "User not found";
        doThrow(new EntityNotFoundException(message)).when(userService)
                .getUser(userId);

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.getUser(userId, authenticatedUser),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void createUser_withAllCorrectInfo_shouldReturnCreatedUser() {
        final CreateUserRequestDto user = buildUserRequestDto();

        when(userService.createUser(user)).thenReturn(buildUserResponseDto(ID));

        final ResponseEntity<UserResponseDto> actual = testObj.createUser(user);

        assertNotNull(actual);
        assertEquals(HttpStatusCode.valueOf(201), actual.getStatusCode());
        assertEquals(ID, actual.getBody()
                .id());
        assertEquals(EMAIL, actual.getBody()
                .email());
        assertEquals(NAME, actual.getBody()
                .name());
        assertEquals(ADDRESS_DTO, actual.getBody()
                .address());
        assertEquals(PHONE_NUMBER, actual.getBody()
                .phoneNumber());
        assertEquals(NOW, actual.getBody()
                .createdTimestamp());
        assertEquals(NOW, actual.getBody()
                .updatedTimestamp());
    }

    @Test
    void createUser_withExistingEmail_shouldReturnUserAlreadyInUse() {
        final CreateUserRequestDto user = buildUserRequestDto();

        final String message = "Email is already in use";
        doThrow(new IllegalArgumentException(
                message)).when(userService)
                .createUser(user);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> testObj.createUser(user),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void updateUser_withAllParams_shouldReturnUpdatedDetails() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);
        final UpdateUserRequestDto user = new UpdateUserRequestDto(NAME, "john1@email.com", null,
                null,
                PASSWORD);

        when(userService.updateUser(userId, user)).thenReturn(
                new UserResponseDto(userId, NAME, ADDRESS_DTO, PHONE_NUMBER, "john1@email.com", NOW,
                        NOW));

        final ResponseEntity<UserResponseDto> actual = testObj.updateUser(userId, user,
                authenticatedUser);

        assertNotNull(actual);

        assertEquals(userId, actual.getBody()
                .id());
        assertEquals(HttpStatusCode.valueOf(200), actual.getStatusCode());
        assertEquals("john1@email.com", actual.getBody()
                .email());
        assertEquals(NAME, actual.getBody()
                .name());
    }

    @Test
    void updateUser_withOtherExistingIdAndAuthenticated_shouldReturnForbidden() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);
        final UpdateUserRequestDto user = new UpdateUserRequestDto(NAME, "john1@email.com", null,
                PASSWORD, null);

        final String message = "You are not allowed to access this resource";
        final AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> testObj.updateUser(generateUserId(), user, authenticatedUser),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void updateUser_withNonExistingIdAndAuthenticated_shouldReturnNotFound() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);
        final String message = "User not found";
        final UpdateUserRequestDto user = new UpdateUserRequestDto(NAME, "john1@email.com", null,
                PASSWORD, null);

        doThrow(new EntityNotFoundException(message)).when(userService)
                .updateUser(userId, user);

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.updateUser(userId, user, authenticatedUser),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void deleteUser_withUserId_shouldReturnNoContent() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);

        doNothing().when(userService)
                .deleteUser(userId);

        final ResponseEntity actual = testObj.deleteUser(userId, authenticatedUser);

        assertNotNull(actual);

        assertEquals(HttpStatusCode.valueOf(204), actual.getStatusCode());
    }

    @Test
    void deleteUser_withOtherExistingIdAndAuthenticated_shouldReturnForbidden() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);

        final String message = "You are not allowed to access this resource";
        final AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> testObj.deleteUser(generateUserId(), authenticatedUser),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void deleteUser_withNonExistingIdAndAuthenticated_shouldReturnNotFound() {
        final String userId = generateUserId();
        final AuthenticatedUser authenticatedUser = buildAuthenticatedUser(userId);

        final String message = "User not found";
        doThrow(new EntityNotFoundException(message)).when(userService)
                .deleteUser(userId);

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.deleteUser(userId, authenticatedUser),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }
}