package com.eaglebank.dto;

public record UpdateBankAccountRequestDto(
        String name,
        AccountType accountType
) {

}
