package com.example.hub.server.it;

import com.sfronc.hub.server.AppConfig;
import com.sfronc.hub.server.Wiring;
import com.sfronc.hub.server.db.DataSourceProvider;
import com.sfronc.hub.server.events.SimpleEventBus;
import com.sfronc.hub.server.http.HttpApiServer;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import com.sfronc.hub.server.repo.JdbcTelemetryRepository;
import com.sfronc.hub.server.repo.TelemetryRepository;
import com.sfronc.hub.server.tcp.TcpServer;

public final class TestHub implements AutoCloseable {
    public final String token = "test-token";
    public final MetricsRegistry metrics = new MetricsRegistry();

    public final TcpServer tcp;
    public final HttpApiServer http;
    public final TelemetryRepository repo;

    public TestHub() throws Exception {
        var cfg = AppConfig.builder()
                .tcpPort(0)
                .httpPort(0)
                .sharedToken(token)
                .jdbcUrl("jdbc:h2:mem:itdb;DB_CLOSE_DELAY=-1")
                .build();

        var ds = DataSourceProvider.h2(cfg.jdbcUrl);
        this.repo = new JdbcTelemetryRepository(ds);

        var dispatcher = Wiring.dispatcher(cfg, repo, new SimpleEventBus(), metrics);

        this.tcp = new TcpServer(cfg, dispatcher, metrics);
        this.http = new HttpApiServer(cfg, repo, metrics);

        tcp.start();
        http.start();
    }

    public int tcpPort() { return tcp.getPort(); }
    public int httpPort() { return http.getPort(); }

    @Override
    public void close() {
        try { http.close(); } catch (Exception ignored) {}
        try { tcp.close(); } catch (Exception ignored) {}
    }
}
