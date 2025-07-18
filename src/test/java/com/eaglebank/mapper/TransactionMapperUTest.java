package com.eaglebank.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.eaglebank.dto.CreateTransactionRequestDto;
import com.eaglebank.dto.TransactionResponseDto;
import com.eaglebank.dto.TransactionType;
import com.eaglebank.model.Transaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionMapperUTest {

    private static final OffsetDateTime NOW = OffsetDateTime.now();
    private static final BigDecimal AMOUNT = BigDecimal.ONE;
    private static final String CURRENCY = "gbp";
    private static final TransactionType TRANSACTION_TYPE = TransactionType.DEPOSIT;
    private static final String REF = "ref";

    @Test
    void toEntity_shouldMapCorrectly() {
        final Transaction actual = TransactionMapper.toEntity(
                new CreateTransactionRequestDto(AMOUNT, CURRENCY,
                        TRANSACTION_TYPE, REF));

        assertEquals(AMOUNT, actual.getAmount());
        assertEquals(CURRENCY, actual.getCurrency());
        assertEquals(TRANSACTION_TYPE, actual.getType());
        assertEquals(REF, actual.getReference());
    }

    @Test
    void toDto_shouldMapCorrectly() {
        final TransactionResponseDto actual = TransactionMapper.toDto(
                new Transaction(AMOUNT, CURRENCY, TRANSACTION_TYPE, REF, NOW));

        assertEquals(AMOUNT, actual.amount());
        assertEquals(CURRENCY, actual.currency());
        assertEquals(TRANSACTION_TYPE, actual.type());
        assertEquals(REF, actual.reference());
    }
}