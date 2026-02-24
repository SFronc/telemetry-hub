package com.sfronc.hub.server.tcp;

import com.sfronc.hub.server.AppConfig;
import com.sfronc.hub.server.dispatch.RequestDispatcher;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

final class NioReactor implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(NioReactor.class);

    private final AppConfig cfg;
    private final RequestDispatcher dispatcher;
    private final MetricsRegistry metrics;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private final SelectorTaskQueue taskQueue = new SelectorTaskQueue();

    private final CountDownLatch boundLatch = new CountDownLatch(1);
    private volatile int boundPort = -1;

    private Selector selector;
    private ServerSocketChannel server;
    private final ExecutorService worker = Executors.newVirtualThreadPerTaskExecutor();

    NioReactor(AppConfig cfg, RequestDispatcher dispatcher, MetricsRegistry metrics) {
        this.cfg = cfg;
        this.dispatcher = dispatcher;
        this.metrics = metrics;
    }

    @Override
    public void run() {
        try (Selector sel = Selector.open(); ServerSocketChannel ssc = ServerSocketChannel.open();) {
            this.selector = sel;
            this.server = ssc;

            ssc.configureBlocking(false);
            ssc.bind(new InetSocketAddress(cfg.tcpPort));
            ssc.register(sel, SelectionKey.OP_ACCEPT);

            ByteBuffer ioBuf = ByteBuffer.allocateDirect(32 * 1024);

            while (running.get()) {
                sel.select(cfg.selectTimeout.toMillis());
                taskQueue.drain();

                Iterator<SelectionKey> it = sel.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isAcceptable()) onAccept(key);
                        if (key.isReadable()) onRead(key, ioBuf);
                        if (key.isWritable()) onWrite(key);
                    }
                    catch (Exception e) {
                        log.warn("Connection error: {}", e.toString());
                        closeKey(key);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Reactor failed: " + e.getMessage(), e);
        }
        finally {
            worker.close();
            log.info("Reactor shutdown");
        }
    }

    void shutdown() {
        running.set(false);
        if (selector != null) selector.wakeup();
    }

    private void onAccept(SelectionKey key) throws IOException {
        var ssc = (ServerSocketChannel) key.channel();
        SocketChannel ch = ssc.accept();
        if (ch == null) return;

        ch.configureBlocking(false);
        ch.socket().setTcpNoDelay(true);

        ConnectionContext ctx = new ConnectionContext(ch, cfg.maxFrameBytes);
        SelectionKey clientKey = ch.register(selector, SelectionKey.OP_READ, ctx);
        ctx.key = clientKey;

        metrics.connectionsIncrement();
        log.info("Accepted {}", ch.getRemoteAddress());
    }

    private void onRead(SelectionKey key, ByteBuffer ioBuf) throws IOException {
        var ch = (SocketChannel) key.channel();
        var ctx = (ConnectionContext) key.attachment();

        ioBuf.clear();
        int read = ch.read(ioBuf);
        if (read < 0) {
            closeKey(key);
            return;
        }
        if (read == 0) return;

        ioBuf.flip();
        for (byte[] frame: ctx.decoder.feed(ioBuf)) {
            dispatcher.handle(frame)
                    .thenApply()
        }
    }

    private void onWrite(SelectionKey key) throws IOException {
        // TODO
    }

    private void closeKey(SelectionKey key) {
        // TODO
    }

}
