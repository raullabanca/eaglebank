package com.eaglebank.controller;

import com.eaglebank.dto.AuthRequestDto;
import com.eaglebank.dto.AuthResponseDto;
import com.eaglebank.exception.InvalidCredentialsException;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.security.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody final AuthRequestDto credentials) {
        final var user = userRepository.findByEmail(credentials.email())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!passwordEncoder.matches(credentials.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        return ResponseEntity.ok(new AuthResponseDto(jwtUtil.generateToken(user)));
    }
}

