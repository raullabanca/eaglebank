package com.eaglebank.model;

import com.eaglebank.dto.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bankAccounts",
        uniqueConstraints = @UniqueConstraint(columnNames = {"accountNumber",
                "sortCode"}))
@Getter
@Setter
@NoArgsConstructor
public class BankAccount {

    @Id
    private String accountNumber;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    private String sortCode;

    private BigDecimal balance;

    private String currency;

    private OffsetDateTime createdTimestamp;

    private OffsetDateTime updatedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public BankAccount(final String name, final AccountType accountType) {
        this.name = name;
        this.accountType = accountType;
    }
}
