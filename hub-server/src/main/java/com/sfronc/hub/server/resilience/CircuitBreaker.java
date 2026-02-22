package com.sfronc.hub.server.resilience;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class CircuitBreaker {
    public enum State {CLOSED, OPEN, HALF_OPEN }

    private final int failureThreshold;
    private final Duration openDuration;

    private final AtomicInteger failures = new AtomicInteger(0);
    private final AtomicLong openUntilEpochMs = new AtomicLong(0);
    private volatile State state = State.CLOSED;

    public CircuitBreaker(int failureThreshold, Duration openDuration) {
        if (failureThreshold <= 0) throw new IllegalArgumentException("failureThreshold must be > 0");
        if (openDuration == null || openDuration.isNegative()) throw new IllegalArgumentException("openDuration must be >= 0");
        this.failureThreshold = failureThreshold;
        this.openDuration = openDuration;
    }

    public State state() {
        if (state == State.OPEN && (System.currentTimeMillis() >= openUntilEpochMs.get())) {
            state = State.HALF_OPEN;
        }
        return state;
    }

    public void beforeCall() {
        State s = state();
        if (s == State.OPEN) {
            throw new IllegalStateException("Circuit breaker is already open");
        }
    }

    public void onSuccess() {
        failures.set(0);
        state = State.CLOSED;
    }

    public void onFailure() {
        int f = failures.incrementAndGet();
        if (f >= failureThreshold) {
            state = State.OPEN;
            openUntilEpochMs.set(System.currentTimeMillis() + openDuration.toMillis());
        }
        else if (state == State.HALF_OPEN) {
            state = State.OPEN;
            openUntilEpochMs.set(System.currentTimeMillis() + openDuration.toMillis());
        }
    }




}
