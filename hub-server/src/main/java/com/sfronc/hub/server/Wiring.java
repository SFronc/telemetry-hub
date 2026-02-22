package com.sfronc.hub.server;

import com.sfronc.hub.server.auth.SharedTokenAuthStrategy;
import com.sfronc.hub.server.dispatch.CommandFactory;
import com.sfronc.hub.server.dispatch.RequestDispatcher;
import com.sfronc.hub.server.events.EventBus;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import com.sfronc.hub.server.repo.TelemetryRepository;
import com.sfronc.hub.server.resilience.CircuitBreaker;
import com.sfronc.hub.server.resilience.ResilientTelemetryRepository;
import com.sfronc.hub.server.resilience.RetryPolicy;

public final class Wiring {
    private Wiring() {}

    public static RequestDispatcher dispatcher(AppConfig cfg, TelemetryRepository repo, EventBus bus, MetricsRegistry metrics) {
        var retry = new RetryPolicy(cfg.retryMaxAttempts, cfg.retryBaseDelay);
        var cb = new CircuitBreaker(cfg.cbFailtureThreshold, cfg.chOpenDuration);

        TelemetryRepository resilientRepo = new ResilientTelemetryRepository(repo, retry, cb);

        var factory = new CommandFactory(resilientRepo, bus, metrics);
        return new RequestDispatcher(factory, new SharedTokenAuthStrategy(cfg.sharedToken), metrics);
    }
}
