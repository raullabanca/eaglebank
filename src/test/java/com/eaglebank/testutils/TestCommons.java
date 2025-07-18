package com.eaglebank.testutils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestCommons {

    public static String toJson(final Object obj)
            throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(obj);
    }
}
