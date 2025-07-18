package com.eaglebank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequestDto(
        @NotBlank
        String name,

        @NotNull
        AddressDto address,

        @NotBlank
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
        String phoneNumber,

        @NotBlank
        @Email
        String email,

        @NotBlank @Size(min = 6) String password
) {

}
