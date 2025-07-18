package com.eaglebank.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequestDto(
        String name,
        @Email String email,
        @Valid AddressDto address,
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
        String phoneNumber,
        @Size(min = 6) String password
) {

}
