package com.example.hub.server.repo;

import com.sfronc.hub.common.messages.TelemetryIngest;
import com.sfronc.hub.server.db.DataSourceProvider;
import com.sfronc.hub.server.repo.JdbcTelemetryRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class JdbcTelemetryRepositoryTest {

    @Test
    void saveAndQuery() {
        var ds = DataSourceProvider.h2("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        var repo = new JdbcTelemetryRepository(ds);

        long now = System.currentTimeMillis();
        repo.save(new TelemetryIngest("dev-1", now, Map.of("cpu", 0.7)));

        var res = repo.query("dev-1", now - 1_000, now + 1_000, 10);
        assertThat(res).hasSize(1);
        assertThat(res.get(0).metrics()).containsEntry("cpu", 0.7);
    }
}
