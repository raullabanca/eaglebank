package com.eaglebank.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateTransactionRequestDto(
        @DecimalMin(value = "0.00") @DecimalMax(value = "10000.00")
        BigDecimal amount,

        @NotNull
        String currency,

        @NotNull
        TransactionType type,

        String reference
) {

}
