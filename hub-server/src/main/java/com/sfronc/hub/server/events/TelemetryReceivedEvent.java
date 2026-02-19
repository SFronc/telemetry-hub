package com.sfronc.hub.server.events;

import com.sfronc.hub.common.messages.TelemetryIngest;

public record TelemetryReceivedEvent(TelemetryIngest ingest) {}
