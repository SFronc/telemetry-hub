package com.sfronc.hub.client;

import com.sfronc.hub.common.messages.TelemetryIngest;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class TelemetryGenerator {
    private TelemetryGenerator() {}

    public static TelemetryIngest random(String deviceId) {
        long now = System.currentTimeMillis();
        double cpu = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
        double mem = ThreadLocalRandom.current().nextDouble(0.0, 1.0);
        double temp = ThreadLocalRandom.current().nextDouble(0.0, 1.0);

        return new TelemetryIngest(deviceId, now, Map.of(
                "cpu", round(cpu),
                "mem", round(mem),
                "tempC", round(temp)
        ));
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
