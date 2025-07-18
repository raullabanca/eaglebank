package com.eaglebank.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.eaglebank.dto.CreateTransactionRequestDto;
import com.eaglebank.dto.TransactionResponseDto;
import com.eaglebank.dto.TransactionType;
import com.eaglebank.security.AuthenticatedUser;
import com.eaglebank.service.TransactionService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TransactionControllerUTest {

    private static final String CURRENCY = "gbp";
    private static final String REFERENCE = "ref";
    private static final String USER_ID = "1L";
    private static final String ACCOUNT_NUMBER = "123456";
    private static final String TRANSACTION_ID = "123456789";
    private static final String EMAIL = "john@email.com";
    private static final AuthenticatedUser AUTHENTICATED_USER = new AuthenticatedUser(USER_ID,
            EMAIL,
            List.of());
    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController testObj;

    @Test
    void createTransaction_withAllParams_shouldReturnTransactionDetails() {
        final CreateTransactionRequestDto createTransactionRequestDto = new CreateTransactionRequestDto(
                BigDecimal.TEN,
                CURRENCY, TransactionType.DEPOSIT,
                REFERENCE);
        final TransactionResponseDto expected = new TransactionResponseDto(TRANSACTION_ID,
                BigDecimal.TEN,
                CURRENCY,
                TransactionType.DEPOSIT, REFERENCE, USER_ID, OffsetDateTime.now());
        when(transactionService.createTransaction(ACCOUNT_NUMBER, USER_ID,
                createTransactionRequestDto)).thenReturn(
                expected);

        final ResponseEntity<TransactionResponseDto> actual = testObj.createTransaction(
                ACCOUNT_NUMBER,
                createTransactionRequestDto, AUTHENTICATED_USER);

        assertEquals(expected, actual.getBody());
    }

    @Test
    void createTransaction_somethingGoesWrong_shouldThrowException() {
        final CreateTransactionRequestDto createTransactionRequestDto = new CreateTransactionRequestDto(
                BigDecimal.TEN,
                CURRENCY, TransactionType.DEPOSIT,
                REFERENCE);

        final String message = "Something went wrong";
        doThrow(new RuntimeException(message)).when(transactionService)
                .createTransaction(any(), any(),
                        any());

        final var actual = assertThrows(RuntimeException.class, () -> testObj.createTransaction(
                ACCOUNT_NUMBER,
                createTransactionRequestDto, AUTHENTICATED_USER), message);

        assertTrue(actual.getMessage()
                .contains(message));
    }

    @Test
    void getTransactions_withAccountNumber_shouldReturnList() {
        final TransactionResponseDto transactionResponseDto = mock();
        final List<TransactionResponseDto> expected = List.of(transactionResponseDto);

        when(transactionService.getTransactions(ACCOUNT_NUMBER, USER_ID)).thenReturn(expected);

        final ResponseEntity<List<TransactionResponseDto>> actual = testObj.getTransactions(
                ACCOUNT_NUMBER, AUTHENTICATED_USER);

        assertEquals(expected, actual.getBody());
    }

    @Test
    void getTransactions_somethingGoesWrong_shouldThrowException() {
        final String message = "Something went wrong";

        doThrow(new RuntimeException(message)).when(transactionService)
                .getTransactions(ACCOUNT_NUMBER, USER_ID);

        final var actual = assertThrows(RuntimeException.class, () -> testObj.getTransactions(
                ACCOUNT_NUMBER, AUTHENTICATED_USER), message);

        assertTrue(actual.getMessage()
                .contains(message));
    }

    @Test
    void getTransaction_withAccountNumber_shouldReturnTransaction() {
        final TransactionResponseDto expected = mock();

        when(transactionService.getTransaction(USER_ID, ACCOUNT_NUMBER, TRANSACTION_ID)).thenReturn(
                expected);

        final ResponseEntity<TransactionResponseDto> actual = testObj.getTransaction(
                ACCOUNT_NUMBER, TRANSACTION_ID, AUTHENTICATED_USER);

        assertEquals(expected, actual.getBody());
    }

    @Test
    void getTransaction_somethingGoesWrong_shouldThrowException() {
        final String message = "Something went wrong";

        doThrow(new RuntimeException(message)).when(transactionService)
                .getTransaction(USER_ID, ACCOUNT_NUMBER, TRANSACTION_ID);

        final var actual = assertThrows(RuntimeException.class, () -> testObj.getTransaction(
                ACCOUNT_NUMBER, TRANSACTION_ID, AUTHENTICATED_USER), message);

        assertTrue(actual.getMessage()
                .contains(message));
    }
}