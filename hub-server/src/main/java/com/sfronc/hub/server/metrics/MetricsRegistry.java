package com.sfronc.hub.server.metrics;

import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class MetricsRegistry {
    private final LongAdder ingested = new LongAdder();
    private final LongAdder errors = new LongAdder();
    private final LongAdder connections = new LongAdder();

    public void ingestIncrement() { ingested.increment(); }
    public void errorsIncrement() { errors.increment(); }
    public void connectionsIncrement() { connections.increment(); }
    public void connectionsDecrement() { connections.decrement(); }

    public Map<String, Long> snapshot() {
        return Map.of(
                "ingested_total", ingested.sum(),
                "errors_total", errors.sum(),
                "connections_current", connections.sum()
        );
    }
}
