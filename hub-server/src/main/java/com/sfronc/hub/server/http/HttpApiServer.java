package com.sfronc.hub.server.http;

import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.TelemetryIngest;
import com.sfronc.hub.server.AppConfig;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import com.sfronc.hub.server.repo.TelemetryRepository;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.h2.result.Sparse;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import static com.sfronc.hub.server.http.HttpUtil.*;

public final class HttpApiServer implements Closeable {
    private final AppConfig cfg;
    private final TelemetryRepository repo;
    private final MetricsRegistry metrics;
    private HttpServer server;
    private volatile  int boundPort = -1;

    public HttpApiServer(AppConfig cfg, final TelemetryRepository repo, MetricsRegistry metrics) {
        this.cfg = cfg;
        this.repo = repo;
        this.metrics = metrics;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(cfg.httpPort), 0);
        boundPort = server.getAddress().getPort();
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.createContext("/health", this::health);
        server.createContext("/metrics", this::metrics);
        server.createContext("/devices", this::devices);
        server.createContext("/ingest", this::ingestHttp);

        server.start();
    }

    public int getPort() {
        return boundPort;
    }

    @Override
    public void close() {
        if (server != null) server.stop(0);
    }

    private void requireAuth(HttpExchange ex) throws IOException {
        if ("/health".equals(ex.getRequestURI().getPath())) return;

        String token = bearerToken(ex);
        if (token == null || token.isBlank()) {
            writeError(ex, 401, "Missing Bearer token");
            throw new AuthStop();
        }
        if (!cfg.sharedToken.equals(token)) {
            throw new AuthStop();
        }
    }

    private void health(HttpExchange ex) throws IOException {
        writeJson(ex, 200, Map.of("status", "UP"));
    }

    private void metrics(HttpExchange ex) throws IOException {
        try {
            requireAuth(ex);
        }
        catch (AuthStop stop) { return; }
        writeJson(ex, 200, metrics.snapshot());
    }

    private void devices(HttpExchange ex) throws IOException {
        try {
            requireAuth(ex);
        }
        catch (AuthStop stop) { return; }

        if(!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            writeError(ex, 405, "Method not allowed");
            return;
        }

        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length < 4 || !"readings".equals(parts[3])) {
            writeError(ex, 404, "Not found");
            return;
        }

        String deviceId = parts[2];
        Map<String, String> q = parseQuery(ex.getRequestURI());

        long now = System.currentTimeMillis();
        long from = parseLong(q.getOrDefault("from", String.valueOf(now - 3600_000)));
        long to = parseLong(q.getOrDefault("to", String.valueOf(now)));
        int limit = (int) parseLong(q.getOrDefault("limit", "100"));

        var readings = repo.query(deviceId, from, to, limit);
        writeJson(ex, 200, Map.of("deviceId", deviceId, "readings", readings));
    }

    private void ingestHttp(HttpExchange ex) throws IOException {
        try {
            requireAuth(ex);
        }
        catch (AuthStop stop) { return; }

        if(!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            writeError(ex, 405, "Method not allowed");
            return;
        }

        byte[] body = readBody(ex, 1_000_000);
        TelemetryIngest ingest = Json.fromBytes(body, TelemetryIngest.class);
        repo.save(ingest);
        metrics.ingestIncrement();

        writeJson(ex, 202, Map.of("status", "ACCEPTED"));
    }

    private static long parseLong(String v) {
        try {
            return Long.parseLong(v);
        }
        catch (Exception e) { throw new IllegalArgumentException("Invalid number: " + v); }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> out = new HashMap<>();
        String q = uri.getRawQuery();
        if (q == null || q.isBlank()) return out;
        for (String kv: q.split("&")) {
            String[] p = kv.split("=", 2);
            String k = decode(p[0]);
            String v = p.length > 1 ? decode(p[1]) : "";
            out.put(k, v);
        }
        return out;
    }

    private static final class AuthStop extends RuntimeException {}
}
