package com.eaglebank.service;

import static com.eaglebank.testutils.UserTestCommons.ID;
import static com.eaglebank.testutils.UserTestCommons.generateUserId;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eaglebank.dto.AccountType;
import com.eaglebank.dto.BankAccountResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.dto.UpdateBankAccountRequestDto;
import com.eaglebank.model.BankAccount;
import com.eaglebank.model.User;
import com.eaglebank.repository.BankAccountRepository;
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
class BankAccountServiceUTest {

    private static final String ACCOUNT_NUMBER = "12345678";
    private static final String ACCOUNT_NAME = "John Doe";
    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private BankAccountService testObj;

    @Test
    void createBankAccount_withAllParams_shouldReturnCreatedBankAccount() {
        final var requestDto = new CreateBankAccountRequestDto(ACCOUNT_NAME, AccountType.PERSONAL);
        final var userId = generateUserId();

        when(bankAccountRepository.existsByAccountNumberAndSortCode(any(), any())).thenReturn(
                false);
        when(bankAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var responseDto = testObj.createBankAccount(requestDto, userId);

        assertNotNull(responseDto);
        assertEquals(ACCOUNT_NAME, responseDto.name());
        assertEquals(AccountType.PERSONAL, responseDto.accountType());
        assertEquals(BigDecimal.ZERO, responseDto.balance());
        assertNotNull(responseDto.accountNumber());
        assertNotNull(responseDto.sortCode());
        assertNotNull(responseDto.createdTimestamp());
        assertNotNull(responseDto.updatedTimestamp());

        verify(bankAccountRepository).save(any());
    }

    @Test
    void createBankAccount_somethingGoesWrong_shouldThrowException() {
        final var requestDto = new CreateBankAccountRequestDto(ACCOUNT_NAME, AccountType.PERSONAL);
        final var message = "Error calling the DB";

        doThrow(new RuntimeException(message)).when(bankAccountRepository)
                .save(any());

        final var actual = assertThrows(RuntimeException.class,
                () -> testObj.createBankAccount(requestDto, generateUserId()), message);

        assertTrue(actual.getMessage()
                .contains(message));
    }

    @Test
    void createBankAccount_duplicateAccountNumberSortCode_shouldGenerateANewValueAndCreate() {
        final var sortCode = "10-10-10";

        when(bankAccountRepository.existsByAccountNumberAndSortCode(any(), eq(sortCode)))
                .thenReturn(true)
                .thenReturn(false);

        final var actual = testObj.generateUniqueAccountNumber(sortCode);

        assertNotNull(actual);
        verify(bankAccountRepository, times(2)).existsByAccountNumberAndSortCode(any(),
                eq(sortCode));
    }

    @Test
    void getBankAccounts_userId_returnsListOfBankAccounts() {
        final var name1 = "personal bank account 1";
        final var name2 = "personal bank account 2";
        final var bankAccount1 = new BankAccount(name1, AccountType.PERSONAL);
        final var bankAccount2 = new BankAccount(name2, AccountType.PERSONAL);
        final var sortCode = "20-20-20";
        final var balance = BigDecimal.ZERO;
        final var now = OffsetDateTime.now();
        final var accountNumber2 = "12345679";
        final var currency = "gbp";

        final var expected1 = new BankAccountResponseDto(ACCOUNT_NUMBER, sortCode, name1,
                AccountType.PERSONAL, balance, currency, now, now);
        final var expected2 = new BankAccountResponseDto(accountNumber2, sortCode, name2,
                AccountType.PERSONAL, balance, currency, now, now);

        bankAccount1.setAccountNumber(ACCOUNT_NUMBER);
        bankAccount1.setSortCode(sortCode);
        bankAccount1.setBalance(balance);
        bankAccount1.setCurrency(currency);
        bankAccount1.setCreatedTimestamp(now);
        bankAccount1.setUpdatedTimestamp(now);

        bankAccount2.setAccountNumber(accountNumber2);
        bankAccount2.setSortCode(sortCode);
        bankAccount2.setBalance(balance);
        bankAccount2.setCurrency(currency);
        bankAccount2.setCreatedTimestamp(now);
        bankAccount2.setUpdatedTimestamp(now);

        when(bankAccountRepository.findAllByUser_Id(ID)).thenReturn(
                List.of(bankAccount1, bankAccount2));

        final List<BankAccountResponseDto> actual = testObj.getBankAccounts(ID);

        assertAll(
                "BankAccountResponses",
                () -> assertTrue(actual
                                .contains(expected1),
                        "Expected BankAccountResponse 1 not found"),
                () -> assertTrue(actual
                                .contains(expected2),
                        "Expected BankAccountResponse 2 not found")

        );
    }

    @Test
    void getBankAccounts_userId_returnsEmptyList() {
        when(bankAccountRepository.findAllByUser_Id(ID)).thenReturn(
                List.of());

        final List<BankAccountResponseDto> actual = testObj.getBankAccounts(ID);

        assertTrue(actual.isEmpty());
    }

    @Test
    void getBankAccounts_somethingGoesWrong_throwsException() {
        final var message = "Something went wrong";
        doThrow(new RuntimeException(message)).when(bankAccountRepository)
                .findAllByUser_Id(ID);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.getBankAccounts(ID),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void getBankAccount_accountNumber_shouldReturnAccountDetails() {
        final String sortCode = "20-20-20";
        final BigDecimal balance = BigDecimal.ZERO;
        final OffsetDateTime now = OffsetDateTime.now();
        final String accountNumber = ACCOUNT_NUMBER;
        final String currency = "gbp";
        final BankAccount bankAccount = new BankAccount(ACCOUNT_NAME, AccountType.PERSONAL);
        final var user = new User();

        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setSortCode(sortCode);
        bankAccount.setBalance(balance);
        bankAccount.setCurrency(currency);
        bankAccount.setCreatedTimestamp(now);
        bankAccount.setUpdatedTimestamp(now);
        user.setId(ID);
        bankAccount.setUser(user);

        when(bankAccountRepository.findById(accountNumber)).thenReturn(Optional.of(bankAccount));

        final BankAccountResponseDto actual = testObj.getBankAccount(accountNumber, ID);

        assertEquals(ACCOUNT_NAME, actual.name());
        assertEquals(AccountType.PERSONAL, actual.accountType());
        assertEquals(balance, actual.balance());
        assertEquals(currency, actual.currency());
        assertEquals(accountNumber, actual.accountNumber());
        assertEquals(sortCode, actual.sortCode());
        assertEquals(now, actual.createdTimestamp());
        assertEquals(now, actual.updatedTimestamp());
    }

    @Test
    void getBankAccount_nonExistingAccountNumber_shouldReturnNotFound() {
        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.getBankAccount(ACCOUNT_NUMBER, ID),
                "Bank account not found"
        );

        assertTrue(exception.getMessage()
                .contains("Bank account not found"));
    }

    @Test
    void getBankAccount_accountNumberOfAnotherMember_shouldReturnForbidden() {
        final String message = "You are not allowed to access this resource.";
        final BankAccount bankAccount = new BankAccount("account name", AccountType.PERSONAL);
        final User user = new User();
        user.setId("3L");
        bankAccount.setUser(user);
        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));

        final AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> testObj.getBankAccount(ACCOUNT_NUMBER, ID),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void updateBankAccount_withParams_shouldReturnUpdatedBankAccount() {
        final var requestDto = new UpdateBankAccountRequestDto(ACCOUNT_NAME, AccountType.PERSONAL);
        final BankAccount bankAccount = new BankAccount();
        final User user = new User();
        user.setId(ID);
        bankAccount.setUser(user);

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));
        when(bankAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        final var responseDto = testObj.updateBankAccount(ACCOUNT_NUMBER, ID, requestDto);

        assertNotNull(responseDto);
        assertEquals(ACCOUNT_NAME, responseDto.name());
        assertEquals(AccountType.PERSONAL, responseDto.accountType());

        verify(bankAccountRepository).save(any());
    }

    @Test
    void updateBankAccount_somethingGoesWrong_shouldReturnThrowException() {
        final var message = "Something went wrong";
        final var requestDto = new UpdateBankAccountRequestDto(ACCOUNT_NAME, AccountType.PERSONAL);
        final BankAccount bankAccount = new BankAccount();
        final User user = new User();
        user.setId(ID);
        bankAccount.setUser(user);
        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));
        doThrow(new RuntimeException(message)).when(bankAccountRepository)
                .save(any());

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.updateBankAccount(ACCOUNT_NUMBER, ID, requestDto),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void updateBankAccount_accountNumberNotFound_shouldReturnThrowException() {
        final var requestDto = new UpdateBankAccountRequestDto(ACCOUNT_NAME, AccountType.PERSONAL);

        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.updateBankAccount(ACCOUNT_NUMBER, ID, requestDto),
                "Bank account not found"
        );

        assertTrue(exception.getMessage()
                .contains("Bank account not found"));
    }

    @Test
    void updateBankAccount_accountNumberOfAnotherMember_shouldReturnForbidden() {
        final String message = "You are not allowed to access this resource.";
        final BankAccount bankAccount = new BankAccount("account name", AccountType.PERSONAL);
        final User user = new User();
        user.setId("3L");
        bankAccount.setUser(user);
        final var requestDto = new UpdateBankAccountRequestDto(ACCOUNT_NAME, AccountType.PERSONAL);
        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));

        final AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> testObj.updateBankAccount(ACCOUNT_NUMBER, ID, requestDto),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void deleteBankAccount_accountNumber_shouldDelete() {
        final BankAccount bankAccount = new BankAccount("account name", AccountType.PERSONAL);
        final User user = new User();
        user.setId(ID);
        bankAccount.setUser(user);
        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));

        testObj.deleteBankAccount(ACCOUNT_NUMBER, ID);

        verify(bankAccountRepository).deleteById(ACCOUNT_NUMBER);
    }

    @Test
    void deleteBankAccount_nonExistingAccountNumber_shouldThrowException() {
        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> testObj.deleteBankAccount(ACCOUNT_NUMBER, ID),
                "Bank account not found"
        );

        assertTrue(exception.getMessage()
                .contains("Bank account not found"));
        verify(bankAccountRepository, times(0)).deleteById(ACCOUNT_NUMBER);
    }

    @Test
    void deleteBankAccount_withAccountNumberOfAnotherUser_shouldThrowException() {
        final String message = "You are not allowed to access this resource.";
        final BankAccount bankAccount = new BankAccount("account name", AccountType.PERSONAL);
        final User user = new User();
        user.setId("3L");
        bankAccount.setUser(user);
        when(bankAccountRepository.findById(ACCOUNT_NUMBER)).thenReturn(Optional.of(bankAccount));

        final AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> testObj.deleteBankAccount(ACCOUNT_NUMBER, ID),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }
}