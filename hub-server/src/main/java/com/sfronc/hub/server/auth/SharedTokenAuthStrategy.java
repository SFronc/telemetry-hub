package com.sfronc.hub.server.auth;

import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;
import com.sfronc.hub.server.exceptions.UnauthorizedException;

import java.util.Objects;

public final class SharedTokenAuthStrategy implements AuthStrategy {
    private final String token;

    public SharedTokenAuthStrategy(String token) {
        this.token = Objects.requireNonNull(token, "token");
    }

    @Override
    public void authorize(MessageEnvelope envelope) {
        if (envelope.type() == MessageType.PING) return;

        String t = envelope.authToken();
        if (t == null || t.isBlank()) throw new UnauthorizedException("Missing authToken");
        if (!token.equals(t)) throw new UnauthorizedException("Invalid authToken");
    }
}
