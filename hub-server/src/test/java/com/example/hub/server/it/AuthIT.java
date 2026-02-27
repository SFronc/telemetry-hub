package com.example.hub.server.it;

import com.sfronc.hub.client.TelemetryGenerator;
import com.sfronc.hub.client.TelemetryTcpClient;
import com.sfronc.hub.common.Ids;
import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.ErrorResponse;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.*;

class AuthIT {

    @Test
    void tcpMissingToken_isUnauthorized() throws Exception {
        try (TestHub hub = new TestHub()) {
            try (TelemetryTcpClient client = new TelemetryTcpClient("127.0.0.1", hub.tcpPort(), null)) {
                var ingest = TelemetryGenerator.random("dev-auth-1");
                var env = new MessageEnvelope(MessageType.INGEST, Ids.correlationId(), Json.toTree(ingest), null);
                var resp = client.request(env);

                assertThat(resp.type()).isEqualTo(MessageType.ERROR);
                ErrorResponse er = Json.treeToValue(resp.payload(), ErrorResponse.class);
                assertThat(er.code()).isEqualTo("UNAUTHORIZED");
            }
        }
    }

    @Test
    void httpMissingBearer_is401() throws Exception {
        try (TestHub hub = new TestHub()) {
            HttpClient hc = HttpClient.newHttpClient();
            URI uri = URI.create("http://127.0.0.1:" + hub.httpPort() + "/metrics");
            HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
            HttpResponse<String> r = hc.send(req, HttpResponse.BodyHandlers.ofString());
            assertThat(r.statusCode()).isEqualTo(401);
        }
    }
}
