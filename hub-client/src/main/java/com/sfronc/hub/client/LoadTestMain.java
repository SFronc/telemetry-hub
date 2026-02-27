package com.sfronc.hub.client;

import com.sfronc.hub.common.Ids;
import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

public final class LoadTestMain {
    public static void main(final String[] args) throws InterruptedException {
        String host = "127.0.0.1";
        int port = Integer.parseInt(System.getenv().getOrDefault("HUB_TCP_PORT", "9000"));
        String token = System.getenv().getOrDefault("HUB_SHARED_TOKEN", "dev-token");
        int threads = Integer.parseInt(System.getenv().getOrDefault("LOAD_THREADS", "50"));
        int perThread = Integer.parseInt(System.getenv().getOrDefault("LOAD_PER_THREAD", "200"));
        String devicePrefix = System.getenv().getOrDefault("LOAD_DEVICE_PREFIX", "load-dev");

        LongAdder ok = new LongAdder();
        LongAdder err = new LongAdder();

        Instant start = Instant.now();
        var exec = Executors.newVirtualThreadPerTaskExecutor();

        for (int t = 0; t < threads; t++) {
            final String deviceId = devicePrefix + "-" + t;
            exec.submit(() -> {
                try (TelemetryTcpClient client = new TelemetryTcpClient(host, port, token)) {
                    for (int i = 0; i < perThread; i++) {
                        var ingest = TelemetryGenerator.random(deviceId);
                        var env = new MessageEnvelope(MessageType.INGEST, Ids.correlationId(), Json.toTree(ingest), token);
                        var resp = client.request(env);
                        if (resp.type() == MessageType.ACK) ok.increment();
                        else err.increment();
                    }
                } catch (Exception e) {
                    err.increment();
                }
            });
        }
        exec.close();
        while (!exec.isTerminated()) {
            Thread.sleep(50);
        }

        Duration d = Duration.between(start, Instant.now());
        long total = ok.sum() + err.sum();
        double rps = total / Math.max(0.001, d.toMillis() / 1000.0);

        System.out.println("Load test done:");
        System.out.println(" threads=" + threads + " perThread=" + perThread);
        System.out.println(" ok=" + ok.sum() + " err=" + err.sum());
        System.out.println(" duration=" + d.toMillis());
        System.out.println(" rps=" + String.format("%.2f", rps));
    }
}
