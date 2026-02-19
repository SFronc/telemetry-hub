package com.sfronc.hub.server.db;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public final class DataSourceProvider {
    private DataSourceProvider() {}

    public static DataSource h2(String jdbcUrl) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL(jdbcUrl);
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }
}
