package com.sfronc.hub.server.tcp;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

final class ConnectionContext {
    final SocketChannel ch;
    final FramedMessageDecoder decoder;
    final Queue<ByteBuffer> outbound = new ConcurrentLinkedQueue<>();
    SelectionKey key;

    ConnectionContext(SocketChannel channel, int maxFrameBytes) {
        this.ch = channel;
        decoder = new FramedMessageDecoder(maxFrameBytes);
    }

    void enqueue(ByteBuffer buf) {
        outbound.add(buf);
    }

    boolean hasOutbound() {
        return !outbound.isEmpty();
    }

    ByteBuffer peekOutbound() {
        return outbound.peek();
    }

    void popOutbound() {
        outbound.poll();
    }
}
