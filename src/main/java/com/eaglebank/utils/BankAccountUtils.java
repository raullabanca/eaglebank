package com.eaglebank.utils;

import java.util.Random;

public class BankAccountUtils {

    private static final Random random = new Random();

    public static String generateAccountNumber() {
        int number = 100000 + random.nextInt(900000);
        return "01" + number;
    }

    public static String generateSortCode() {
        return String.format("%02d-%02d-%02d",
                random.nextInt(100),
                random.nextInt(100),
                random.nextInt(100));
    }
}
