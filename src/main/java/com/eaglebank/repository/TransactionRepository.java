package com.eaglebank.repository;

import com.eaglebank.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findAllByBankAccount_AccountNumber(String accountNumber);
}
