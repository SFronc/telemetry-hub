package com.sfronc.hub.server.dispatch;

import com.sfronc.hub.common.protocol.MessageEnvelope;

public interface Command {
    MessageEnvelope execute();
}
