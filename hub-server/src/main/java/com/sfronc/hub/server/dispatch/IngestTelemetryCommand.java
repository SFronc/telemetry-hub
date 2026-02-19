package com.sfronc.hub.server.dispatch;

import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.Ack;
import com.sfronc.hub.common.messages.TelemetryIngest;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;
import com.sfronc.hub.server.events.EventBus;
import com.sfronc.hub.server.events.TelemetryReceivedEvent;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import com.sfronc.hub.server.repo.TelemetryRepository;

class IngestTelemetryCommand implements Command {
    private final MessageEnvelope req;
    private final TelemetryIngest ingest;
    private final TelemetryRepository repo;
    private final EventBus bus;
    private final MetricsRegistry metrics;

    IngestTelemetryCommand(MessageEnvelope req, TelemetryIngest ingest, TelemetryRepository repo, EventBus bus, MetricsRegistry metrics) {
        this.req = req;
        this.ingest = ingest;
        this.repo = repo;
        this.bus = bus;
        this.metrics = metrics;
    }

    @Override
    public MessageEnvelope execute() {
        repo.save(ingest);
        bus.publish(new TelemetryReceivedEvent(ingest));
        metrics.ingestIncrement();

        var ack = new Ack(req.correlationId(), "OK");
        return new MessageEnvelope(MessageType.ACK, req.correlationId(), Json.toTree(ack));
    }
}
