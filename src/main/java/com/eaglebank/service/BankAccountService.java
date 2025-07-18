package com.eaglebank.service;

import static com.eaglebank.utils.BankAccountUtils.generateAccountNumber;
import static com.eaglebank.utils.BankAccountUtils.generateSortCode;

import com.eaglebank.dto.BankAccountResponseDto;
import com.eaglebank.dto.CreateBankAccountRequestDto;
import com.eaglebank.dto.UpdateBankAccountRequestDto;
import com.eaglebank.mapper.BankAccountMapper;
import com.eaglebank.model.BankAccount;
import com.eaglebank.model.User;
import com.eaglebank.repository.BankAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BankAccountService {

    private static final String CURRENCY = "GBP";
    private final BankAccountRepository bankAccountRepository;

    public List<BankAccountResponseDto> getBankAccounts(final String userId) {
        return bankAccountRepository.findAllByUser_Id(userId)
                .stream()
                .map(BankAccountMapper::toDto)
                .toList();
    }

    public BankAccountResponseDto createBankAccount(final CreateBankAccountRequestDto bankAccountRequestDto,
            final String userId) {
        final String sortCode = generateSortCode();

        final BankAccount bankAccount = BankAccountMapper.toEntity(bankAccountRequestDto);
        final User user = new User();

        user.setId(userId);
        bankAccount.setAccountNumber(generateUniqueAccountNumber(sortCode));
        bankAccount.setSortCode(sortCode);
        bankAccount.setBalance(BigDecimal.ZERO);
        bankAccount.setCreatedTimestamp(OffsetDateTime.now());
        bankAccount.setUpdatedTimestamp(OffsetDateTime.now());
        bankAccount.setCurrency(CURRENCY);
        bankAccount.setUser(user);

        return BankAccountMapper.toDto(
                bankAccountRepository.save(bankAccount));
    }

    public BankAccountResponseDto getBankAccount(final String accountNumber, final String userId) {
        final BankAccount bankAccount = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Bank account not found"));

        if (!userId.equals(bankAccount.getUser()
                .getId())) {
            throw new AccessDeniedException("You are not allowed to access this resource.");
        }

        return BankAccountMapper.toDto(bankAccount);
    }

    public BankAccountResponseDto updateBankAccount(final String accountNumber,
            final String userId,
            final
            UpdateBankAccountRequestDto bankAccountRequest) {
        final BankAccount bankAccount = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Bank account not found"));

        if (!userId.equals(bankAccount.getUser()
                .getId())) {
            throw new AccessDeniedException("You are not allowed to access this resource.");
        }

        if (bankAccountRequest.name() != null) {
            bankAccount.setName(bankAccountRequest.name());
        }

        if (bankAccountRequest.accountType() != null) {
            bankAccount.setAccountType(bankAccountRequest.accountType());
        }

        bankAccount.setUpdatedTimestamp(OffsetDateTime.now());

        return BankAccountMapper.toDto(bankAccountRepository.save(bankAccount));
    }

    public void deleteBankAccount(final String accountNumber, final String userId) {
        final BankAccount bankAccount = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Bank account not found"));

        if (!userId.equals(bankAccount.getUser()
                .getId())) {
            throw new AccessDeniedException("You are not allowed to access this resource.");
        }

        bankAccountRepository.deleteById(accountNumber);
    }

    public String generateUniqueAccountNumber(final String sortCode) {
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (bankAccountRepository.existsByAccountNumberAndSortCode(accountNumber, sortCode));
        return accountNumber;
    }
}
