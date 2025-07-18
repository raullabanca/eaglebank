package com.eaglebank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDto(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
) {

}
