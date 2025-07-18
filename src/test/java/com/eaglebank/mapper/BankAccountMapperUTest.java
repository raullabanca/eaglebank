package com.eaglebank.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.eaglebank.dto.AccountType;
import com.eaglebank.dto.BankAccountResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.model.BankAccount;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankAccountMapperUTest {

    private final static String personalBankAccountName = "Personal bank account";

    @Test
    void toEntity_shouldMapCorrectly() {

        final BankAccount actual = BankAccountMapper.toEntity(
                new CreateBankAccountRequestDto(personalBankAccountName, AccountType.PERSONAL));

        assertEquals(personalBankAccountName, actual.getName());
        assertEquals(AccountType.PERSONAL, actual.getAccountType());
        assertNull(actual.getBalance());
        assertNull(actual.getAccountNumber());
        assertNull(actual.getSortCode());
        assertNull(actual.getCreatedTimestamp());
        assertNull(actual.getUpdatedTimestamp());
    }

    @Test
    void toDto_shouldMapCorrectly() {
        final String sortCode = "20-20-20";
        final BigDecimal balance = BigDecimal.ZERO;
        final OffsetDateTime now = OffsetDateTime.now();
        final String accountNumber = "12345678";
        final String currency = "gbp";
        final BankAccount bankAccount = new BankAccount(personalBankAccountName,
                AccountType.PERSONAL);

        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setSortCode(sortCode);
        bankAccount.setBalance(balance);
        bankAccount.setCurrency(currency);
        bankAccount.setCreatedTimestamp(now);
        bankAccount.setUpdatedTimestamp(now);
        final BankAccountResponseDto actual = BankAccountMapper.toDto(bankAccount);

        assertEquals(personalBankAccountName, actual.name());
        assertEquals(AccountType.PERSONAL, actual.accountType());
        assertEquals(balance, actual.balance());
        assertEquals(currency, actual.currency());
        assertEquals(accountNumber, actual.accountNumber());
        assertEquals(sortCode, actual.sortCode());
        assertEquals(now, actual.createdTimestamp());
        assertEquals(now, actual.updatedTimestamp());
    }
}