package com.eaglebank.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    DEPOSIT, WITHDRAWAL;

    @JsonValue
    public String toLowerCase() {
        return name().toLowerCase();
    }
}