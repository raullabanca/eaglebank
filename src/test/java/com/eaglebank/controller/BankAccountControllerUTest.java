package com.eaglebank.controller;

import static com.eaglebank.testutils.UserTestCommons.EMAIL;
import static com.eaglebank.testutils.UserTestCommons.ID;
import static com.eaglebank.testutils.UserTestCommons.generateUserId;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eaglebank.dto.AccountType;
import com.eaglebank.dto.BankAccountResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.dto.UpdateBankAccountRequestDto;
import com.eaglebank.security.AuthenticatedUser;
import com.eaglebank.service.BankAccountService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class BankAccountControllerUTest {

    public static final String PERSONAL_BANK_ACCOUNT = "personal bank account 1";
    private static final AuthenticatedUser AUTH_USER = new AuthenticatedUser(ID, EMAIL, List.of());
    private static final String ACCOUNT_NUMBER = "12345678";
    @Mock
    private BankAccountService bankAccountService;

    @InjectMocks
    private BankAccountController testObj;

    @Test
    void createBankAccount_withAllCorrectArgs_shouldReturnCreatedBankAccountDetails() {
        final BankAccountResponseDto expected = new BankAccountResponseDto("123456", "20-20-20",
                PERSONAL_BANK_ACCOUNT, AccountType.PERSONAL, BigDecimal.ZERO, "gbp",
                OffsetDateTime.now(), OffsetDateTime.now());
        final String userId = generateUserId();
        when(bankAccountService.createBankAccount(
                new CreateBankAccountRequestDto(PERSONAL_BANK_ACCOUNT, AccountType.PERSONAL),
                userId))
                .thenReturn(expected);

        final ResponseEntity<BankAccountResponseDto> actual = testObj.createBankAccount(
                new CreateBankAccountRequestDto(PERSONAL_BANK_ACCOUNT, AccountType.PERSONAL),
                new AuthenticatedUser(userId, EMAIL,
                        List.of()));

        assertNotNull(actual);
        assertEquals(expected.name(), actual.getBody()
                .name());
        assertEquals(expected.accountType(), actual.getBody()
                .accountType());
        assertEquals(expected.balance(), actual.getBody()
                .balance());
        assertEquals(expected.currency(), actual.getBody()
                .currency());
        assertEquals(expected.createdTimestamp(), actual.getBody()
                .createdTimestamp());
        assertEquals(expected.updatedTimestamp(), actual.getBody()
                .updatedTimestamp());
    }

    @Test
    void createBankAccount_somethingGoesWrong_shouldThrowException() {
        final String message = "somethingWentWrong";
        final String userId = generateUserId();

        doThrow(new RuntimeException(message)).when(bankAccountService)
                .createBankAccount(
                        new CreateBankAccountRequestDto(PERSONAL_BANK_ACCOUNT,
                                AccountType.PERSONAL),
                        userId);

        final var actual = assertThrows(RuntimeException.class, () -> testObj.createBankAccount(
                new CreateBankAccountRequestDto(PERSONAL_BANK_ACCOUNT, AccountType.PERSONAL),
                new AuthenticatedUser(userId, EMAIL,
                        List.of())), message);

        assertTrue(actual.getMessage()
                .contains(message));
    }

    @Test
    void getBankAccounts_accountNumber_returnsListOfBankAccounts() {
        final var name2 = "personal bank account 2";
        final var sortCode = "20-20-20";
        final var balance = BigDecimal.ZERO;
        final var now = OffsetDateTime.now();
        final var accountNumber2 = "12345679";
        final var currency = "gbp";

        final var expected1 = new BankAccountResponseDto(ACCOUNT_NUMBER, sortCode,
                PERSONAL_BANK_ACCOUNT,
                AccountType.PERSONAL, balance, currency, now, now);
        final var expected2 = new BankAccountResponseDto(accountNumber2, sortCode, name2,
                AccountType.PERSONAL, balance, currency, now, now);

        when(bankAccountService.getBankAccounts(ID)).thenReturn(
                List.of(expected1, expected2));

        final ResponseEntity<List<BankAccountResponseDto>> actual = testObj.getBankAccounts(
                AUTH_USER);

        assertAll(
                "BankAccountResponses",
                () -> assertTrue(actual.getBody()
                                .contains(expected1),
                        "Expected BankAccountResponse 1 not found"),
                () -> assertTrue(actual.getBody()
                                .contains(expected2),
                        "Expected BankAccountResponse 2 not found")

        );
    }

    @Test
    void getBankAccounts_accountNumber_returnsEmptyList() {
        when(bankAccountService.getBankAccounts(ID)).thenReturn(
                List.of());

        final ResponseEntity<List<BankAccountResponseDto>> actual = testObj.getBankAccounts(
                AUTH_USER);

        assertTrue(actual.getBody()
                .isEmpty());
    }

    @Test
    void getBankAccounts_somethingGoesWrong_throwsException() {
        final var message = "Something went wrong";
        doThrow(new RuntimeException(message)).when(bankAccountService)
                .getBankAccounts(ID);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.getBankAccounts(AUTH_USER),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void getBankAccount_accountNumber_returnsListOfBankAccounts() {
        final var sortCode = "20-20-20";
        final var balance = BigDecimal.ZERO;
        final var now = OffsetDateTime.now();
        final var currency = "gbp";

        final var expected = new BankAccountResponseDto(ACCOUNT_NUMBER, sortCode,
                PERSONAL_BANK_ACCOUNT,
                AccountType.PERSONAL, balance, currency, now, now);

        when(bankAccountService.getBankAccount(ACCOUNT_NUMBER, ID)).thenReturn(
                expected);

        final ResponseEntity<BankAccountResponseDto> actual = testObj.getBankAccount(ACCOUNT_NUMBER,
                AUTH_USER);

        assertEquals(expected, actual.getBody());
    }

    @Test
    void getBankAccount_nonExistingAccountNumber_returnsNotFound() {
        final String message = "Not found";
        doThrow(new EntityNotFoundException(message)).when(bankAccountService)
                .getBankAccount(
                        ACCOUNT_NUMBER, ID);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.getBankAccount(ACCOUNT_NUMBER,
                        AUTH_USER),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void getBankAccount_somethingGoesWrong_throwsException() {
        final var message = "Something went wrong";
        doThrow(new RuntimeException(message)).when(bankAccountService)
                .getBankAccount(ACCOUNT_NUMBER, ID);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.getBankAccount(ACCOUNT_NUMBER,
                        AUTH_USER),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void updateBankAccount_withParams_shouldReturnUpdatedUser() {
        final UpdateBankAccountRequestDto personalAccount = new UpdateBankAccountRequestDto(
                PERSONAL_BANK_ACCOUNT, null);
        final BankAccountResponseDto expected = new BankAccountResponseDto(ACCOUNT_NUMBER, null,
                PERSONAL_BANK_ACCOUNT, null, null, null, null, null);
        when(bankAccountService.updateBankAccount(ACCOUNT_NUMBER, ID,
                personalAccount))
                .thenReturn(expected);

        final ResponseEntity<BankAccountResponseDto> actual = testObj.updateBankAccount(
                ACCOUNT_NUMBER,
                personalAccount,
                new AuthenticatedUser(ID, EMAIL,
                        List.of()));

        assertNotNull(actual);
        assertEquals(expected.name(), actual.getBody()
                .name());
        assertEquals(expected.accountType(), actual.getBody()
                .accountType());
        assertEquals(expected.balance(), actual.getBody()
                .balance());
        assertEquals(expected.currency(), actual.getBody()
                .currency());
        assertEquals(expected.createdTimestamp(), actual.getBody()
                .createdTimestamp());
        assertEquals(expected.updatedTimestamp(), actual.getBody()
                .updatedTimestamp());
    }

    @Test
    void updateBankAccount_somethingWentWrong_throwsException() {
        final var message = "Something went wrong";
        final var updateBankAccountRequestDto = new UpdateBankAccountRequestDto(
                PERSONAL_BANK_ACCOUNT, null);
        doThrow(new RuntimeException(message)).when(bankAccountService)
                .updateBankAccount(ACCOUNT_NUMBER, ID, updateBankAccountRequestDto);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.updateBankAccount(ACCOUNT_NUMBER,
                        updateBankAccountRequestDto,
                        new AuthenticatedUser(ID, EMAIL,
                                List.of())),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }

    @Test
    void deleteBankAccount_accountNumber_shouldDelete() {
        doNothing().when(bankAccountService)
                .deleteBankAccount(ACCOUNT_NUMBER, ID);

        final ResponseEntity actual = testObj.deleteBankAccount(ACCOUNT_NUMBER, AUTH_USER);

        verify(bankAccountService).deleteBankAccount(ACCOUNT_NUMBER, AUTH_USER.getId());
        assertEquals(HttpStatusCode.valueOf(204), actual.getStatusCode());
    }

    @Test
    void deleteBankAccount_somethingGoesWrong_shouldThrowException() {
        final var message = "Something went wrong";
        doThrow(new RuntimeException(message)).when(bankAccountService)
                .deleteBankAccount(ACCOUNT_NUMBER, ID);

        final RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> testObj.deleteBankAccount(ACCOUNT_NUMBER, AUTH_USER),
                message
        );

        assertTrue(exception.getMessage()
                .contains(message));
    }
}