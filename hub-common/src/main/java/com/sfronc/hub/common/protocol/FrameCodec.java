package com.sfronc.hub.common.protocol;

import com.sfronc.hub.common.Json;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class FrameCodec {
    private static final int LEN_BYTES = 4;

    private FrameCodec() {}

    public static ByteBuffer encode(MessageEnvelope envelope) {
        byte[] body = Json.toBytes(envelope);
        ByteBuffer out = ByteBuffer.allocate(LEN_BYTES + body.length).order(ByteOrder.BIG_ENDIAN);
        out.putInt(body.length);
        out.put(body);
        out.flip();
        return out;
    }

    public static MessageEnvelope decode(byte[] frameBytes) {
        return Json.fromBytes(frameBytes, MessageEnvelope.class);
    }
}
