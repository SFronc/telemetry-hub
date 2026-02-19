package com.sfronc.hub.server.repo;

import com.sfronc.hub.common.messages.TelemetryIngest;
import com.sfronc.hub.common.messages.TelemetryReading;

import java.util.List;

public interface TelemetryRepository {
    void save(TelemetryIngest ingest);
    List<TelemetryReading> query(String deviceId, long fromEpochMs, long toEpochMs, int limit);
}
