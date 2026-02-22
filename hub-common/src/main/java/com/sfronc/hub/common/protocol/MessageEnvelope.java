package com.sfronc.hub.common.protocol;

import com.fasterxml.jackson.databind.JsonNode;

public record MessageEnvelope (
        MessageType type,
        String correlationId,
        JsonNode payload,
        String authToken
) {}
