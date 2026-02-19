package com.sfronc.hub.server.resilience;

import com.sfronc.hub.server.repo.TelemetryRepository;

public final class ResilientTelemetryRepository implements TelemetryRepository {
    private final TelemetryRepository delegate;
    private final RetryP
}
