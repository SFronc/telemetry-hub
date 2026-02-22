package com.sfronc.hub.server.auth;

import com.sfronc.hub.common.protocol.MessageEnvelope;

public interface AuthStrategy {
    void authorize(MessageEnvelope envelope);
}
