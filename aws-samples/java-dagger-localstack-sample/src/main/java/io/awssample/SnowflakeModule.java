package io.awssample;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Module;
import dagger.Provides;
import io.awssample.persistence.ProductRepository;
import io.awssample.persistence.SnowflakeProductRepository;

import javax.inject.Singleton;

@Module
public class SnowflakeModule {

    // FIXME: use application.properties
    static {
        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Provides
    @Singleton
    HikariConfig provideHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:snowflake://snowflake.localhost.localstack.cloud:4567");
        config.setUsername("test");
        config.setPassword("test");
        config.setDriverClassName("net.snowflake.client.jdbc.SnowflakeDriver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);
        return config;
    }

    @Provides
    @Singleton
    HikariDataSource provideHikariDataSource(HikariConfig config) {
        return new HikariDataSource(config);
    }

    @Provides
    @Singleton
    public static ProductRepository snowflakeProductRepository(HikariDataSource hikariDataSource) {
        return new SnowflakeProductRepository(hikariDataSource);
    }
}
