package com.eaglebank.repository;

import com.eaglebank.model.BankAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    boolean existsByAccountNumberAndSortCode(String accountNumber, String sortCode);

    List<BankAccount> findAllByUser_Id(String userId);
}
