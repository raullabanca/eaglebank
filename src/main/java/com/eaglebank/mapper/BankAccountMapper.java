package com.eaglebank.mapper;

import com.eaglebank.dto.BankAccountResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.model.BankAccount;

public class BankAccountMapper {

    public static BankAccount toEntity(final CreateBankAccountRequestDto dto) {
        return new BankAccount(dto.name(), dto.accountType());
    }

    public static BankAccountResponseDto toDto(final BankAccount bankAccount) {
        return new BankAccountResponseDto(bankAccount.getAccountNumber(), bankAccount.getSortCode(),
                bankAccount.getName(), bankAccount.getAccountType(), bankAccount.getBalance(),
                bankAccount.getCurrency(), bankAccount.getCreatedTimestamp(),
                bankAccount.getUpdatedTimestamp());
    }
}
