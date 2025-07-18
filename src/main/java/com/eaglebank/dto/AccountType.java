package com.eaglebank.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountType {
    PERSONAL;

    @JsonValue
    public String toLowerCase() {
        return name().toLowerCase();
    }
}
