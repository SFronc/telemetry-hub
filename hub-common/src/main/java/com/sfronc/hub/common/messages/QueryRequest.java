package com.sfronc.hub.common.messages;

import com.sfronc.hub.common.Validation;

public record QueryRequest(
        String deviceId,
        long fromEpochMs,
        long toEpochMs,
        int limit
) {
    public QueryRequest {
        Validation.requireNonBlank(deviceId, "deviceId");
        Validation.requirePositiveOrZero(fromEpochMs, "fromEpochMs");
        Validation.requirePositiveOrZero(toEpochMs, "toEpochMs");
        if (limit <= 0 || limit > 10_000) {
            throw new IllegalArgumentException("limit must be between 0 and 1000");
        }
        if (toEpochMs < fromEpochMs) {
            throw new IllegalArgumentException("toEpochMs must be greater than fromEpochMs");
        }
    }
}
