package com.sfronc.hub.server;

import java.time.Duration;

public final class AppConfig {
    public final int tcpPort;
    public final int httpPort;
    public final String jdbcUrl;
    public final int maxFrameBytes;
    public final Duration selectTimeout;

    private AppConfig(Builder builder) {
        this.tcpPort = builder.tcpPort;
        this.httpPort = builder.httpPort;
        this.jdbcUrl = builder.jdbcUrl;
        this.maxFrameBytes = builder.maxFrameBytes;
        this.selectTimeout = builder.selectTimeout;
    }

    public static Builder builder() { return  new Builder(); }

    public static final class Builder {
        private int tcpPort = 9000;
        private int httpPort = 8080;
        private String jdbcUrl = "jdbc:h2:mem:telemetry;DB_CLOSE_DELAY=-1";
        private int maxFrameBytes = 1_000_000;
        private Duration selectTimeout = Duration.ofMillis(500);

        public Builder tcpPort(int v) { this.tcpPort = v; return this; }
        public Builder httpPort(int v) { this.httpPort = v; return this; }
        public Builder jdbcUrl(String v) { this.jdbcUrl = v; return this; }
        public Builder maxFrameBytes(int v) { this.maxFrameBytes = v; return this; }
        public Builder selectTimeout(Duration v) { this.selectTimeout = v; return this; }

        public AppConfig build() { return new AppConfig(this); }
    }
}
