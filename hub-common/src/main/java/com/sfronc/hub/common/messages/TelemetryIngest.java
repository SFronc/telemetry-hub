package com.sfronc.hub.common.messages;

import com.sfronc.hub.common.Validation;

import java.util.Map;

public record TelemetryIngest(
    String deviceId,
    long timestampEpochMs,
    Map<String, Double> metrics
) {
    public TelemetryIngest {
        Validation.requireNonBlank(deviceId, "deviceId");
        Validation.requirePositiveOrZero(timestampEpochMs, "timestampEpochMs");
        Validation.requireNonEmpty(metrics, "metrics");
    }
}
