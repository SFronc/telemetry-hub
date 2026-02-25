package com.sfronc.hub.server;

import com.sfronc.hub.server.db.DataSourceProvider;
import com.sfronc.hub.server.events.EventBus;
import com.sfronc.hub.server.events.SimpleEventBus;
import com.sfronc.hub.server.http.HttpApiServer;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import com.sfronc.hub.server.repo.JdbcTelemetryRepository;
import com.sfronc.hub.server.repo.TelemetryRepository;
import com.sfronc.hub.server.tcp.TcpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        var cfg = AppConfig.builder().build();

        var metrics = new MetricsRegistry();
        var ds = DataSourceProvider.h2(cfg.jdbcUrl);
        TelemetryRepository repo = new JdbcTelemetryRepository(ds);

        EventBus bus =  new SimpleEventBus();
        var dispatcher = Wiring.dispatcher(cfg, repo, bus, metrics);

        var tcp = new TcpServer(cfg, dispatcher, metrics);
        var http = new HttpApiServer(cfg, repo, metrics);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown requested...");
            try { http.close(); } catch (Exception ignored) {}
            try { tcp.close(); } catch (Exception ignored) {}
        }, "shutdown-hook"));

        tcp.start();
        http.start();

        log.info("TelemetryHub started. TCP:{} HTTP:{} token={}", tcp.getPort(), http.getPort(), cfg.sharedToken);
    }
}
