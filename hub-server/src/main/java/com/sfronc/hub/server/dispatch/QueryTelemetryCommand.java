package com.sfronc.hub.server.dispatch;

import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.QueryRequest;
import com.sfronc.hub.common.messages.QueryResponse;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;
import com.sfronc.hub.server.repo.TelemetryRepository;

public class QueryTelemetryCommand implements Command {
    private final MessageEnvelope req;
    private final QueryRequest query;
    private final TelemetryRepository repo;

    public QueryTelemetryCommand(MessageEnvelope req, QueryRequest query, TelemetryRepository repo) {
        this.req = req;
        this.query = query;
        this.repo = repo;
    }

    @Override
    public MessageEnvelope execute() {
        var readings = repo.query(query.deviceId(), query.fromEpochMs(), query.toEpochMs(), query.limit());
        var resp = new QueryResponse(req.correlationId(), query.deviceId(), readings);
        return new MessageEnvelope(MessageType.QUERY_RESPONSE, req.correlationId(), Json.toTree(resp));
    }
}
