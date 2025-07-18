package com.eaglebank.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BankAccountResponseDto(
        String accountNumber,
        String sortCode,
        String name,
        AccountType accountType,
        BigDecimal balance,
        String currency,
        OffsetDateTime createdTimestamp,
        OffsetDateTime updatedTimestamp
) {

}
