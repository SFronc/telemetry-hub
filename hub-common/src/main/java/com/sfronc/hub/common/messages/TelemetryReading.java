package com.sfronc.hub.common.messages;

import java.util.Map;

public record TelemetryReading(
        long timestampEpochMs,
        Map<String, Double> metrics
) {}
