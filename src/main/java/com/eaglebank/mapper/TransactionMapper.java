package com.eaglebank.mapper;

import com.eaglebank.dto.CreateTransactionRequestDto;
import com.eaglebank.dto.TransactionResponseDto;
import com.eaglebank.model.Transaction;
import java.time.OffsetDateTime;

public class TransactionMapper {

    public static Transaction toEntity(
            final CreateTransactionRequestDto dto) {
        return new Transaction(
                dto.amount(),
                dto.currency(),
                dto.type(),
                dto.reference(),
                OffsetDateTime.now()
        );
    }

    public static TransactionResponseDto toDto(final Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getType(),
                transaction.getReference(),
                transaction.getUserId(),
                transaction.getCreatedTimestamp()
        );
    }
}
