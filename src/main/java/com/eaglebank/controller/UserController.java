package com.eaglebank.controller;

import com.eaglebank.dto.CreateUserRequestDto;
import com.eaglebank.dto.UpdateUserRequestDto;
import com.eaglebank.dto.UserResponseDto;
import com.eaglebank.security.AuthenticatedUser;
import com.eaglebank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping(path = "/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable final String userId,
            @AuthenticationPrincipal AuthenticatedUser authUser) {
        checkUserIdIsPresentAndValid(userId);

        if (!userId.equals(authUser.getId())) {
            throw new AccessDeniedException("You are not allowed to access this resource.");
        }
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody final CreateUserRequestDto user) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.createUser(user));
    }

    @PatchMapping(path = "{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable final String userId,
            @Valid @RequestBody final UpdateUserRequestDto user,
            @AuthenticationPrincipal AuthenticatedUser authUser) {
        checkUserIdIsPresentAndValid(userId);

        if (!userId.equals(authUser.getId())) {
            throw new AccessDeniedException("You are not allowed to access this resource.");
        }

        return ResponseEntity.ok(userService.updateUser(userId, user));
    }

    @DeleteMapping(path = "{userId}")
    public ResponseEntity deleteUser(@PathVariable final String userId,
            @AuthenticationPrincipal AuthenticatedUser authUser) {
        checkUserIdIsPresentAndValid(userId);

        if (!userId.equals(authUser.getId())) {
            throw new AccessDeniedException("You are not allowed to access this resource.");
        }

        userService.deleteUser(userId);

        return ResponseEntity.noContent()
                .build();
    }

    private void checkUserIdIsPresentAndValid(final String userId) {
        if (userId == null || !userId.matches("^usr-[A-Za-z0-9]{12}$")) {
            throw new IllegalArgumentException("userId is null");
        }
    }
}

