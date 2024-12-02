package io.example.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class Datasource {

    private static class DataSourceHolder {

        private static final HikariDataSource ds;

        static {
            var props = new Properties();
            try {
                props.load(Datasource.class.getClassLoader().getResourceAsStream("datasource.properties"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            var configuration = new HikariConfig(props);
            configuration.setMaximumPoolSize(Runtime.getRuntime().availableProcessors());
            ds = new HikariDataSource(configuration);
        }
    }

    private Datasource() {
        // private constructor to prevent instantiation
    }

    public static Connection getConnection() throws SQLException {
        return DataSourceHolder.ds.getConnection();
    }
}
