package com.eaglebank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.eaglebank.dto.CreateTransactionRequestDto;
import com.eaglebank.dto.TransactionResponseDto;
import com.eaglebank.dto.TransactionType;
import com.eaglebank.model.BankAccount;
import com.eaglebank.model.Transaction;
import com.eaglebank.model.User;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TransactionServiceUTest {

    private static final BigDecimal AMOUNT = BigDecimal.ONE;
    private static final String CURRENCY = "gbp";
    private static final String REFERENCE = "ref";
    private static final OffsetDateTime NOW = OffsetDateTime.now();
    private static final String ACCOUNT_NUMBER = "1890231";
    private static final String USER_ID = "1L";
    private static final String TRANSACTION_ID = "1234";
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService testObj;

    @Test
    void createTransaction_withAlLParamsAndDeposit_shouldReturnTransaction() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        bankAccount.setCurrency(CURRENCY);

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));
        when(transactionRepository.save(any())).thenReturn(mock(Transaction.class));
        when(bankAccountRepository.save(bankAccount)).thenReturn(mock(BankAccount.class));

        final TransactionResponseDto actual = testObj.createTransaction(ACCOUNT_NUMBER, USER_ID,
                new CreateTransactionRequestDto(AMOUNT, CURRENCY, TransactionType.DEPOSIT,
                        REFERENCE));

        assertEquals(AMOUNT, actual.amount());
        assertEquals(CURRENCY, actual.currency());
        assertEquals(TransactionType.DEPOSIT, actual.type());
        assertEquals(REFERENCE, actual.reference());
    }

    @Test
    void createTransaction_withAllParamsAndWithdraw_shouldReturnTransaction() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        bankAccount.setCurrency(CURRENCY);

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));
        when(transactionRepository.save(any())).thenReturn(mock(Transaction.class));
        when(bankAccountRepository.save(bankAccount)).thenReturn(mock(BankAccount.class));

        final TransactionResponseDto actual = testObj.createTransaction(ACCOUNT_NUMBER, USER_ID,
                new CreateTransactionRequestDto(AMOUNT, CURRENCY, TransactionType.WITHDRAWAL,
                        REFERENCE));

        assertEquals(AMOUNT, actual.amount());
        assertEquals(CURRENCY, actual.currency());
        assertEquals(TransactionType.WITHDRAWAL, actual.type());
        assertEquals(REFERENCE, actual.reference());
    }

    @Test
    void createTransaction_accountNotFound_shouldThrowException() {
        final var message = "Account not found";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.empty());

        final var actual = assertThrows(EntityNotFoundException.class,
                () -> testObj.createTransaction(ACCOUNT_NUMBER, USER_ID,
                        new CreateTransactionRequestDto(AMOUNT, CURRENCY,
                                TransactionType.WITHDRAWAL,
                                REFERENCE)));

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository, times(0)).save(any());
        verify(bankAccountRepository, times(0)).save(any());
    }

    @Test
    void createTransaction_withNoFundsForWithdraw_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(BigDecimal.ZERO);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        bankAccount.setCurrency(CURRENCY);
        final var message = "Insufficient funds";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));

        final var actual = assertThrows(RuntimeException.class,
                () -> testObj.createTransaction(ACCOUNT_NUMBER, USER_ID,
                        new CreateTransactionRequestDto(AMOUNT, CURRENCY,
                                TransactionType.WITHDRAWAL,
                                REFERENCE)));

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository, times(0)).save(any());
        verify(bankAccountRepository, times(0)).save(any());
    }

    @Test
    void createTransaction_withInvalidCurrency_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        bankAccount.setCurrency("USD");
        final var message = "Currency mismatch";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));

        final var actual = assertThrows(IllegalArgumentException.class,
                () -> testObj.createTransaction(ACCOUNT_NUMBER, USER_ID,
                        new CreateTransactionRequestDto(AMOUNT, CURRENCY,
                                TransactionType.WITHDRAWAL,
                                REFERENCE)));

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository, times(0)).save(any());
        verify(bankAccountRepository, times(0)).save(any());
    }

    @Test
    void createTransaction_somethingGoesWrongSavingTransaction_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        bankAccount.setCurrency(CURRENCY);
        final var message = "Something went wrong";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));
        doThrow(new RuntimeException(message)).when(transactionRepository)
                .save(any());

        final var actual = assertThrows(RuntimeException.class,
                () -> testObj.createTransaction(ACCOUNT_NUMBER, USER_ID,
                        new CreateTransactionRequestDto(AMOUNT, CURRENCY,
                                TransactionType.WITHDRAWAL,
                                REFERENCE)));

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository).save(any());
        verify(bankAccountRepository, times(0)).save(any());
    }

    @Test
    void createTransaction_somethingGoesWrongSavingBankAccount_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        bankAccount.setCurrency(CURRENCY);
        final var message = "Something went wrong";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));
        when(transactionRepository.save(any())).thenReturn(mock(Transaction.class));
        doThrow(new RuntimeException(message)).when(bankAccountRepository)
                .save(any());

        final var actual = assertThrows(RuntimeException.class,
                () -> testObj.createTransaction(ACCOUNT_NUMBER, USER_ID,
                        new CreateTransactionRequestDto(AMOUNT, CURRENCY,
                                TransactionType.WITHDRAWAL,
                                REFERENCE)));

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository).save(any());
        verify(bankAccountRepository).save(any());
    }

    @Test
    void createTransaction_withADifferentUserId_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId("3L");
        bankAccount.setUser(user);
        bankAccount.setCurrency(CURRENCY);
        final var message = "You are not allowed to transact on this account";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));

        final var actual = assertThrows(AccessDeniedException.class,
                () -> testObj.createTransaction(ACCOUNT_NUMBER, USER_ID,
                        new CreateTransactionRequestDto(AMOUNT, CURRENCY,
                                TransactionType.WITHDRAWAL,
                                REFERENCE)));

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository, times(0)).save(any());
        verify(bankAccountRepository, times(0)).save(any());
    }

    @Test
    void getTransactions_accountNumber_shouldReturnList() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));
        when(transactionRepository.findAllByBankAccount_AccountNumber(ACCOUNT_NUMBER)).thenReturn(
                List.of(new Transaction(AMOUNT, CURRENCY, TransactionType.DEPOSIT, REFERENCE,
                        NOW)));

        final List<TransactionResponseDto> actual = testObj.getTransactions(ACCOUNT_NUMBER,
                USER_ID);

        assertNotNull(actual);
        assertFalse(actual.isEmpty());
        assertEquals(AMOUNT, actual.get(0)
                .amount());
        assertEquals(CURRENCY, actual.get(0)
                .currency());
        assertEquals(TransactionType.DEPOSIT, actual.get(0)
                .type());
        assertEquals(REFERENCE, actual.get(0)
                .reference());
    }

    @Test
    void getTransactions_accountNotFound_shouldThrowException() {
        final var message = "Account not found";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.empty());

        final var actual = assertThrows(EntityNotFoundException.class,
                () -> testObj.getTransactions(ACCOUNT_NUMBER,
                        USER_ID));

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getTransactions_withADifferentUserId_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        final var message = "You are not allowed to transact on this account";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));

        final var actual = assertThrows(AccessDeniedException.class,
                () -> testObj.getTransactions(ACCOUNT_NUMBER, "3L"),
                message);

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getTransactions_somethingGoesWrongReadingFromDB_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        final var message = "You are not allowed to transact on this account";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));
        doThrow(new RuntimeException(message)).when(transactionRepository)
                .findAllByBankAccount_AccountNumber(ACCOUNT_NUMBER);

        final var actual = assertThrows(RuntimeException.class,
                () -> testObj.getTransactions(ACCOUNT_NUMBER, USER_ID), message);

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository).findAllByBankAccount_AccountNumber(ACCOUNT_NUMBER);
    }

    @Test
    void getTransaction_userIdAccountNumberAndTransactionId_shouldReturnTransaction() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        bankAccount.setAccountNumber(ACCOUNT_NUMBER);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        final Transaction transaction = new Transaction(AMOUNT, CURRENCY, TransactionType.DEPOSIT,
                REFERENCE,
                NOW);
        transaction.setId(TRANSACTION_ID);
        transaction.setBankAccount(bankAccount);

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));

        final TransactionResponseDto actual = testObj.getTransaction(USER_ID, ACCOUNT_NUMBER,
                TRANSACTION_ID);

        assertNotNull(actual);
        assertEquals(AMOUNT, actual.amount());
        assertEquals(CURRENCY, actual.currency());
        assertEquals(TransactionType.DEPOSIT, actual.type());
        assertEquals(REFERENCE, actual.reference());
    }

    @Test
    void getTransaction_nonExistingBankAccount_shouldThrowException() {
        final var message = "Account not found";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.empty());

        final var actual = assertThrows(EntityNotFoundException.class,
                () -> testObj.getTransaction(USER_ID, ACCOUNT_NUMBER,
                        TRANSACTION_ID));

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getTransaction_withADifferentUserId_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        final var message = "You are not allowed to access this account";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(
                Optional.of(bankAccount));

        final var actual = assertThrows(AccessDeniedException.class,
                () -> testObj.getTransaction("3L", ACCOUNT_NUMBER, TRANSACTION_ID),
                message);

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getTransaction_nonExistingTransaction_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);

        final var message = "Transaction not found";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.empty());

        final var actual = assertThrows(EntityNotFoundException.class,
                () -> testObj.getTransaction(USER_ID, ACCOUNT_NUMBER, TRANSACTION_ID),
                message);

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository).findById(TRANSACTION_ID);
    }

    @Test
    void getTransaction_transactionIdFromAnotherAccount_shouldThrowException() {
        final BankAccount bankAccount = new BankAccount();
        bankAccount.setBalance(AMOUNT);
        bankAccount.setAccountNumber("55L");
        final User user = new User();
        user.setId(USER_ID);
        bankAccount.setUser(user);
        final Transaction transaction = new Transaction(AMOUNT, CURRENCY, TransactionType.DEPOSIT,
                REFERENCE,
                NOW);
        transaction.setId(TRANSACTION_ID);
        transaction.setBankAccount(bankAccount);
        final var message = "Transaction does not belong to the specified account";

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));
        when(transactionRepository.findById(TRANSACTION_ID)).thenReturn(Optional.of(transaction));

        final var actual = assertThrows(EntityNotFoundException.class,
                () -> testObj.getTransaction(USER_ID, ACCOUNT_NUMBER,
                        TRANSACTION_ID), message);

        assertTrue(actual.getMessage()
                .contains(message));

        verify(bankAccountRepository).findById(ACCOUNT_NUMBER);
        verify(transactionRepository).findById(TRANSACTION_ID);
    }
}