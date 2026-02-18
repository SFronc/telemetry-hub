package com.sfronc.hub.common.messages;

import java.util.List;

public record QueryResponse (
    String correlationId,
    String deviceId,
    List<TelemetryReading> readings
) {}
