package com.sfronc.hub.common;

import java.util.Map;

public final class Validation {
    private Validation() {}

    public static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
        return value;
    }

    public static <K, V> Map<K, V> requireNonEmpty(Map<K, V> value, String field) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return value;
    }

    public static long requirePositiveOrZero(long value, String field) {
        if (value < 0) {
            throw new IllegalArgumentException(field + " cannot be negative");
        }
        return value;
    }
}
