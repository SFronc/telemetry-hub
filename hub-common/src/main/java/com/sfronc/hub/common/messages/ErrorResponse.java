package com.sfronc.hub.common.messages;

public record ErrorResponse(
        String correlationId,
        String code,
        String message
) {}
