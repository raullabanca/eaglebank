package com.eaglebank.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionResponseDto(
        String id,
        BigDecimal amount,
        String currency,
        TransactionType type,
        String reference,
        String userId,
        OffsetDateTime createdTimestamp
) {

}

