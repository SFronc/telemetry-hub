package com.sfronc.hub.server.tcp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

final class FramedMessageDecoder {
    private static final int LEN_BYTES = 4;

    private final int maxFrameBytes;
    private ByteBuffer buf = ByteBuffer.allocate(64 * 1024).order(ByteOrder.BIG_ENDIAN);
    private int expectedLen = -1;

    FramedMessageDecoder(int maxFrameBytes) {
        this.maxFrameBytes = maxFrameBytes;
    }

    List<byte[]> feed(ByteBuffer incoming) {
        ensureCapacity(incoming.remaining());
        buf.put(incoming);

        buf.flip();
        List<byte[]> frames = new ArrayList<>();

        while (true) {
            if (expectedLen < 0) {
                if (buf.remaining() < LEN_BYTES) break;
                expectedLen = buf.getInt();
                if (expectedLen <= 0 || expectedLen > maxFrameBytes) {
                    throw new IllegalArgumentException("Invalid frame length: " + expectedLen);
                }
            }
            if (buf.remaining() < expectedLen) break;

            byte[] frame = new byte[expectedLen];
            buf.get(frame);
            frames.add(frame);
            expectedLen = -1;
        }

        buf.compact();
        return frames;
    }

    private void ensureCapacity(int incomingBytes) {
        if (buf.remaining() >= incomingBytes) return;;
        int needed = buf.position() + incomingBytes;
        int newCap = Math.max(buf.capacity() * 2, needed);
        ByteBuffer newBuf = ByteBuffer.allocate(newCap).order(ByteOrder.BIG_ENDIAN);
        buf.flip();
        newBuf.put(buf);
        buf = newBuf;
    }
}
