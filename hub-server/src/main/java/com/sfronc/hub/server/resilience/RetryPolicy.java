package com.sfronc.hub.server.resilience;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

public final class RetryPolicy {
    public final int maxAttempts;
    public final Duration baseDelay;

    public RetryPolicy(final int maxAttempts, final Duration baseDelay) {
        if (maxAttempts <= 0) throw new IllegalArgumentException("maxAttempts must be > 0");
        if (baseDelay == null || baseDelay.isNegative()) throw new IllegalArgumentException("baseDelay must be >= 0");
        this.maxAttempts = maxAttempts;
        this.baseDelay = baseDelay;
    }

    Duration delayForAttempt(int attemptIndex1Based) {
        long baseMs = baseDelay.toMillis();
        long exp = baseMs * (1L << Math.min(20, attemptIndex1Based - 1));
        long jitter = ThreadLocalRandom.current().nextLong(0, Math.max(1, baseMs + 1));
        return Duration.ofMillis(Math.min(5_000, exp + jitter));
    }
}
