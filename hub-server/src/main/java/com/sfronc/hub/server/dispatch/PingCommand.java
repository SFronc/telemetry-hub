package com.sfronc.hub.server.dispatch;

import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.Ack;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;

final class PingCommand implements Command {
    private final MessageEnvelope req;

    PingCommand(MessageEnvelope req) {
        this.req = req;
    }

    public MessageEnvelope execute() {
        var ack = new Ack(req.correlationId(), "PONG");
        return new MessageEnvelope(MessageType.ACK, req.correlationId(), Json.toTree(ack));
    }
}
