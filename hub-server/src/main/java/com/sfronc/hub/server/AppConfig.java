package com.sfronc.hub.server;

import java.time.Duration;

public final class AppConfig {
    public final int tcpPort;
    public final int httpPort;
    public final String jdbcUrl;
    public final int maxFrameBytes;
    public final Duration selectTimeout;
    public final String sharedToken;
    public final int retryMaxAttempts;
    public final Duration retryBaseDelay;
    public final int cbFailtureThreshold;
    public final Duration chOpenDuration;

    private AppConfig(Builder builder) {
        this.tcpPort = builder.tcpPort;
        this.httpPort = builder.httpPort;
        this.jdbcUrl = builder.jdbcUrl;
        this.maxFrameBytes = builder.maxFrameBytes;
        this.selectTimeout = builder.selectTimeout;
        this.sharedToken = builder.sharedToken;
        this.retryMaxAttempts = builder.retryMaxAttempts;
        this.retryBaseDelay = builder.retryBaseDelay;
        this.cbFailtureThreshold = builder.cbFailtureThreshold;
        this.chOpenDuration = builder.cbOpenDuration;
    }

    public static Builder builder() { return  new Builder(); }

    public static final class Builder {
        private int tcpPort = envInt("HUB_TCP_PORT", 9000);
        private int httpPort = envInt("HUB_HTTP_PORT", 8080);
        private String jdbcUrl = System.getenv().getOrDefault("HUB_JDBC_URL","jdbc:h2:mem:telemetry;DB_CLOSE_DELAY=-1");
        private int maxFrameBytes = envInt("HUB_MAX_FRAME_BYTES", 1_000_000);
        private Duration selectTimeout = Duration.ofMillis(envLong("HUB_SELECT_TIMEOUT_MS", 500));
        private String sharedToken = System.getenv().getOrDefault("HUB_SHARED_TOKEN","dev-token");
        private int retryMaxAttempts = envInt("HUB_RETRY_MAX_ATTEMPTS", 3);
        private Duration retryBaseDelay = Duration.ofMillis(envLong("HUB_RETRY_BASE_DELAY", 40));
        private int cbFailtureThreshold = envInt("HUB_CB_FAILURE_THRESHOLD", 5);
        private Duration cbOpenDuration = Duration.ofMillis(envLong("HUB_CB_OPEN_DURATION", 2000));


        public Builder tcpPort(int v) { this.tcpPort = v; return this; }
        public Builder httpPort(int v) { this.httpPort = v; return this; }
        public Builder jdbcUrl(String v) { this.jdbcUrl = v; return this; }
        public Builder maxFrameBytes(int v) { this.maxFrameBytes = v; return this; }
        public Builder selectTimeout(Duration v) { this.selectTimeout = v; return this; }
        public Builder sharedToken(String v) { this.sharedToken = v; return this; }
        public Builder retryMaxAttempts(int v) { this.retryMaxAttempts = v; return this; }
        public Builder retryBaseDelay(Duration v) { this.retryBaseDelay = v; return this; }
        public Builder cbFailtureThreshold(int v) { this.cbFailtureThreshold = v; return this; }
        public Builder cbOpenDuration(Duration v) { this.cbOpenDuration = v; return this; }

        public AppConfig build() { return new AppConfig(this); }

        private static int envInt(String key, int def) {
            String value = System.getenv(key);
            if (value == null || value.isBlank()) return def;
            try {
                return Integer.parseInt(value.trim());
            }
            catch (Exception e) {
                return def;
            }
        }

        private static long envLong(String key, long def) {
            String value = System.getenv(key);
            if (value == null || value.isBlank()) return def;
            try {
                return Long.parseLong(value.trim());
            }
            catch (Exception e) {
                return def;
            }
        }
    }
}
