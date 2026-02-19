package com.sfronc.hub.server.repo;

import com.sfronc.hub.common.Json;
import com.sfronc.hub.common.messages.TelemetryIngest;
import com.sfronc.hub.common.messages.TelemetryReading;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class JdbcTelemetryRepository implements TelemetryRepository {
    private final DataSource ds;

    public JdbcTelemetryRepository(DataSource ds) {
        this.ds = ds;
        initSchema();
    }

    private void initSchema() {
        try (
                Connection c = ds.getConnection();
                Statement st = c.createStatement()
        ) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS telemetry (
                id IDENTITY PRIMARY KEY,
                device_id VARCHAR(200) NOT NULL,
                ts BIGINT NOT NULL,
                metrics_json CLOB NOT NULL
                );
                """);
            st.execute("CREATE INDEX IF NOT EXISTS idx_telemetry_devices_ts ON telemetry(device_id, ts);");
        }
        catch (SQLException e) {
            throw new RuntimeException("DB init failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void save(TelemetryIngest ingest) {
        try (Connection c = ds.getConnection();
        PreparedStatement ps = c.prepareStatement(
                "INSERT INTO telemetry(device_id, ts, metrics_json) VALUES(?,?,?)"
        )) {
            ps.setString(1, ingest.deviceId());
            ps.setLong(2, ingest.timestampEpochMs());
            ps.setString(3, new String(Json.toBytes(ingest.metrics())));
            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("DB insert failed: " + e.getMessage(), e);
        }
    }

    public List<TelemetryReading> query(String deviceId, long fromEpochMs, long toEpochMs, int limit) {
        String sql = """
                SELECT ts, metrics_json
                FROM telemetry
                WHERE device_id = ?
                AND ts BETWEEN ? AND ?
                ORDER BY ts DESC
                LIMIT ?
                """;

        try (Connection c = ds.getConnection();
            PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, deviceId);
            ps.setLong(2, fromEpochMs);
            ps.setLong(3, toEpochMs);
            ps.setInt(4, limit);

            try (ResultSet rs = ps.executeQuery()) {
                List<TelemetryReading> out = new ArrayList<>();
                while (rs.next()) {
                    long ts = rs.getLong(1);
                    String metricsJson = rs.getString(2);

                    @SuppressWarnings("unchecked")
                    Map<String, Double> metrics = Json.getMapper().readValue(metricsJson, Map.class);

                    out.add(new TelemetryReading(ts, metrics));
                }
                return out;
            }
        }
        catch (Exception e) {
            throw new RuntimeException("DB query failed: " + e.getMessage(), e);
        }
    }
}
