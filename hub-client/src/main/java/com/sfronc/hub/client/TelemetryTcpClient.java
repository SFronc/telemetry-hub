package com.sfronc.hub.client;

import com.sfronc.hub.common.Ids;
import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.protocol.FrameCodec;
import com.sfronc.hub.common.protocol.MessageEnvelope;
import com.sfronc.hub.common.protocol.MessageType;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public final class TelemetryTcpClient implements Closeable {
    private final SocketChannel channel;
    private final String token;

    public TelemetryTcpClient(String host, int port, String token) throws IOException {
        this.token = token;
        this.channel = SocketChannel.open();
        channel.connect(new InetSocketAddress(host, port));
        channel.configureBlocking(true);
    }

    public MessageEnvelope ping() throws IOException {
        var env = new MessageEnvelope(MessageType.PING, Ids.correlationId(), Json.toTree(null), null);
        return request(env);
    }

    public MessageEnvelope request(MessageEnvelope env) throws IOException {
        if (env.authToken() == null && token != null) {
            env = new MessageEnvelope(env.type(), env.correlationId(), env.payload(), token);
        }

        ByteBuffer out = FrameCodec.encode(env);

        // TODO
    }
}
