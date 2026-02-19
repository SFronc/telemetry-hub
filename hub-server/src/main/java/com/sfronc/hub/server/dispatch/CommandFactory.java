package com.sfronc.hub.server.dispatch;

import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.QueryRequest;
import com.sfronc.hub.common.messages.TelemetryIngest;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.server.events.EventBus;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import com.sfronc.hub.server.repo.TelemetryRepository;

public final class CommandFactory {
    private final TelemetryRepository repo;
    private final EventBus bus;
    private final MetricsRegistry metrics;

    public CommandFactory(TelemetryRepository repo, EventBus bus, MetricsRegistry metrics) {
        this.repo = repo;
        this.bus = bus;
        this.metrics = metrics;
    }

    public Command create(MessageEnvelope envelope) {
        return switch (envelope.type()) {
            case PING -> new PingCommand(envelope);
            case INGEST -> {
                TelemetryIngest ingest = Json.treeToValue(envelope.payload(), TelemetryIngest.class);
                yield new IngestTelemetryCommand(envelope, ingest, repo, bus, metrics);
            }
            case QUERY -> {
                QueryRequest qr = Json.treeToValue(envelope.payload(), QueryRequest.class);
                yield new QueryTelemetryCommand(envelope, qr, repo);
            }
            default -> throw new IllegalArgumentException("Unknown envelope type: " + envelope.type());
        };
    }
}
