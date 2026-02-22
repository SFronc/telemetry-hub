package com.sfronc.hub.server.resilience;

import com.sfronc.hub.common.messages.TelemetryIngest;
import com.sfronc.hub.common.messages.TelemetryReading;
import com.sfronc.hub.server.exceptions.ServiceUnavailableException;
import com.sfronc.hub.server.repo.TelemetryRepository;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

public final class ResilientTelemetryRepository implements TelemetryRepository {
    private final TelemetryRepository delegate;
    private final RetryPolicy retry;
    private final CircuitBreaker cb;

    public ResilientTelemetryRepository(TelemetryRepository delegate, RetryPolicy retry, CircuitBreaker cb) {
        this.delegate = delegate;
        this.retry = retry;
        this.cb = cb;
    }

    @Override
    public void save(TelemetryIngest ingest) {
        runWithResilience(() -> {
           delegate.save(ingest);
           return null;
        });
    }

    public List<TelemetryReading> query(String deviceId, long fromEpochMs, long toEpochMs, int limit) {
        return runWithResilience(() -> delegate.query(deviceId, fromEpochMs, toEpochMs, limit));
    }

    private <T> T runWithResilience(Supplier<T> supplier) {
        cbGate();

        RuntimeException last = null;
        for (int attempt = 1; attempt <= retry.maxAttempts; attempt++) {
            try {
                T out = supplier.get();
                cb.onSuccess();
                return out;
            }
            catch (RuntimeException e) {
                last = e;
                cb.onFailure();
                if (attempt >= retry.maxAttempts) break;

                sleep(retry.delayForAttempt(attempt));
                cbGate();
            }
        }
        throw last;
    }

    private void cbGate() {
        try {
            cb.beforeCall();
        }
        catch (IllegalStateException e) {
            throw new ServiceUnavailableException("Database circuit breaker is OPEN");
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        }
        catch (InterruptedException ignored) {}
    }
}
