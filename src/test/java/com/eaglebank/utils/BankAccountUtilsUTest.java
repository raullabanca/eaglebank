package com.eaglebank.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankAccountUtilsUTest {

    @Test
    void generateAccountNumber_shouldReturnFixedValue() {
        final String actual = BankAccountUtils.generateAccountNumber();

        assertNotNull(actual);
        assertEquals(8, actual.length());
        assertTrue(actual.startsWith("01"));
        assertTrue(actual.matches("^01\\d{6}$"));
    }

    @Test
    void generateSortCode_shouldReturnFixedValue() {
        String actual = BankAccountUtils.generateSortCode();

        assertNotNull(actual);
        assertEquals(8, actual.length());
        assertTrue(actual.matches("\\d{2}-\\d{2}-\\d{2}"));
    }
}