package com.sfronc.hub.common.messages;

public record Ack(
        String correlationId,
        String status
) {}
