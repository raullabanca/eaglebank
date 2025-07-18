package com.eaglebank.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateBankAccountRequestDto(
        @NotBlank String name,
        @NotNull AccountType accountType
) {

}
