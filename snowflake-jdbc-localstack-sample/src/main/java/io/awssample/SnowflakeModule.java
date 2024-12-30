package io.awssample;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SnowflakeModule {

    // FIXME: use application.properties

    HikariDataSource provideHikariDataSource() {
        HikariConfig config = new HikariConfig();
//        config.setJdbcUrl("jdbc:snowflake://localhost:4566");
        config.setJdbcUrl("jdbc:snowflake://snowflake.localhost.localstack.cloud:4567");
        config.setUsername("test");
        config.setPassword("test");
        config.setDriverClassName("net.snowflake.client.jdbc.SnowflakeDriver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        return new HikariDataSource(config);
    }

}
