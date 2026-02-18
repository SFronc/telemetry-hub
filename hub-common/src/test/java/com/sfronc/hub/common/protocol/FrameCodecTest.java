package com.sfronc.hub.common.protocol;


import com.sfronc.hub.common.Ids;
import com.sfronc.hub.common.Json;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.*;

public class FrameCodecTest {

    @Test
    void encodeDecode_roundTrip() {
        var env = new MessageEnvelope(
                MessageType.PING,
                Ids.correlationId(),
                Json.toTree(new Object() { public final String x = "y"; })
        );

        ByteBuffer bb = FrameCodec.encode(env);

        int len = bb.getInt();
        byte[] body = new byte[len];
        bb.get(body);

        var decoded = FrameCodec.decode(body);

        assertThat(decoded.type()).isEqualTo(env.type());
        assertThat(decoded.correlationId()).isEqualTo(env.correlationId());
        assertThat(decoded.payload().get("x").asText()).isEqualTo("y");
    }
}
