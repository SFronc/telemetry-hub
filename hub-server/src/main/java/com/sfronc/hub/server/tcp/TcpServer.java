package com.sfronc.hub.server.tcp;

import com.sfronc.hub.server.AppConfig;
import com.sfronc.hub.server.dispatch.RequestDispatcher;
import com.sfronc.hub.server.metrics.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

public class TcpServer implements Closeable {
    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private final NioReactor reactor;
    private final Thread thread;

    public TcpServer(AppConfig config, RequestDispatcher dispatcher, MetricsRegistry metrics) {
        this.reactor = new NioReactor(config, dispatcher, metrics);
        this.thread = new Thread(reactor, "nio-reactor");
    }

    public void start() {
        thread.start();
        try {
            reactor.awaitBound(2, TimeUnit.SECONDS);
        }
        catch (InterruptedException ignored) {}
        log.info("TCP server starting on port {} ...", reactor.getBoundPort());
    }

    public int getPort() {
        return reactor.getBoundPort();
    }

    @Override
    public void close() {
        reactor.shutdown();
        try { thread.join(2_000); } catch (InterruptedException ignored) {}
        log.info("TCP server stopped");
    }
}
