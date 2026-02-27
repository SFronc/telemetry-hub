package com.sfronc.hub.client;

import com.sfronc.hub.common.Ids;
import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class TelemetryClientMain {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = Integer.parseInt(System.getenv().getOrDefault("HUB_TCP_PORT", "9000"));
        String token = System.getenv().getOrDefault("HUB_SHARED_TOKEN", "dev-token");

        try (TelemetryTcpClient client = new TelemetryTcpClient(host, port, token)) {
            System.out.println("PING => " + client.ping().type());

            var exec = Executors.newVirtualThreadPerTaskExecutor();

            for (int i = 0; i < 5; i++) {
                String deviceId = "dev-" + (i + 1);
                exec.submit(() -> {
                    for (int n = 0; n < 50; n++) {
                        var ingest = TelemetryGenerator.random(deviceId);
                        var env = new MessageEnvelope(
                                MessageType.INGEST,
                                Ids.correlationId(),
                                Json.toTree(ingest),
                                token
                        );

                        MessageEnvelope resp = null;
                        try {
                            resp = client.request(env);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        if (resp.type() != MessageType.ACK) {
                            System.out.println("Unexpected: " + resp.type() + " payload=" + resp.payload());
                        }
                        sleep(100);
                    }
                });
            }

            exec.close();
            exec.awaitTermination(10, TimeUnit.SECONDS);
            System.out.println("Done.");
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException ignored) {}
    }
}
