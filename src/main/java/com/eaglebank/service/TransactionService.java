package com.eaglebank.service;

import com.eaglebank.dto.CreateTransactionRequestDto;
import com.eaglebank.dto.TransactionResponseDto;
import com.eaglebank.dto.TransactionType;
import com.eaglebank.exception.UnprocessableEntityException;
import com.eaglebank.mapper.TransactionMapper;
import com.eaglebank.model.BankAccount;
import com.eaglebank.model.Transaction;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransactionResponseDto createTransaction(final String accountNumber, final String userId,
            CreateTransactionRequestDto dto) {
        final BankAccount account = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        validatePayload(userId, dto, account);

        if (dto.type() == TransactionType.DEPOSIT) {
            account.setBalance(account.getBalance()
                    .add(dto.amount()));
        } else {
            account.setBalance(account.getBalance()
                    .subtract(dto.amount()));
        }

        account.setUpdatedTimestamp(OffsetDateTime.now());

        final String transactionId = "tan-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6);
        final Transaction transaction = TransactionMapper.toEntity(dto);

        transaction.setId(transactionId);
        transaction.setUserId(userId);
        transaction.setBankAccount(account);

        transactionRepository.save(transaction);
        bankAccountRepository.save(account);

        return TransactionMapper.toDto(transaction);
    }

    public List<TransactionResponseDto> getTransactions(String accountNumber, String userId) {
        final BankAccount account = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (!account.getUser()
                .getId()
                .equals(userId)) {
            throw new AccessDeniedException("You are not allowed to transact on this account");
        }

        final List<Transaction> transactions = transactionRepository.findAllByBankAccount_AccountNumber(
                accountNumber);

        return transactions.stream()
                .map(TransactionMapper::toDto)
                .toList();
    }

    public TransactionResponseDto getTransaction(String userId,
            String accountNumber,
            String transactionId) {
        final BankAccount account = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (!account.getUser()
                .getId()
                .equals(userId)) {
            throw new AccessDeniedException("You are not allowed to access this account");
        }

        final Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        if (!transaction.getBankAccount()
                .getAccountNumber()
                .equals(accountNumber)) {
            throw new EntityNotFoundException(
                    "Transaction does not belong to the specified account");
        }

        return TransactionMapper.toDto(transaction);
    }

    private void validatePayload(final String userId,
            final CreateTransactionRequestDto dto,
            final BankAccount account) {
        if (!account.getUser()
                .getId()
                .equals(userId)) {
            throw new AccessDeniedException("You are not allowed to transact on this account");
        }

        if (!account.getCurrency()
                .equals(dto.currency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        if (dto.type() == TransactionType.WITHDRAWAL &&
            account.getBalance()
                    .compareTo(dto.amount()) < 0) {
            throw new UnprocessableEntityException("Insufficient funds");
        }
    }
}

