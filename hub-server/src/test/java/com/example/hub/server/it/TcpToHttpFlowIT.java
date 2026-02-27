package com.example.hub.server.it;

import com.sfronc.hub.client.TelemetryGenerator;
import com.sfronc.hub.common.Ids;
import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import com.sfronc.hub.client.TelemetryTcpClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class TcpToHttpFlowIT {

    @Test
    void tcpIngest_thenHttpQuery() throws Exception {
        try (TestHub hub = new TestHub()) {
            try (TelemetryTcpClient client = new TelemetryTcpClient("127.0.0.1", hub.tcpPort(), hub.token)) {
                var ingest = TelemetryGenerator.random("dev-it-1");
                var env = new MessageEnvelope(MessageType.INGEST, Ids.correlationId(), Json.toTree(ingest), hub.token);
                var resp = client.request(env);
                assertThat(resp.type()).isEqualTo(MessageType.ACK);
            }

            HttpClient hc = HttpClient.newHttpClient();
            URI uri = URI.create("http://127.0.0.1:" + hub.tcpPort() + "/devices/dev-it-1/readings?limit=10");
            HttpRequest req = HttpRequest.newBuilder(uri)
                    .header("Authorization", "Bearer " + hub.token)
                    .GET()
                    .build();

            HttpResponse<String> r = hc.send(req, HttpResponse.BodyHandlers.ofString());
            assertThat(r.statusCode()).isEqualTo(200);
            assertThat(r.body()).contains("dev-it-1");
        }
    }
}
