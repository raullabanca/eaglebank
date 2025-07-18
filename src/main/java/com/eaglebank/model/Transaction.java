package com.eaglebank.model;

import com.eaglebank.dto.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    private String id;

    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String reference;

    private String userId;

    private OffsetDateTime createdTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number", referencedColumnName = "accountNumber", nullable = false)
    private BankAccount bankAccount;

    public Transaction(final BigDecimal amount, final String currency, final TransactionType type,
            final String reference, final OffsetDateTime createdTimestamp) {
        this.amount = amount;
        this.currency = currency;
        this.type = type;
        this.reference = reference;
        this.createdTimestamp = createdTimestamp;
    }
}

