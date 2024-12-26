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

    @Provides
    @Singleton
    HikariConfig provideHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:snowflake://localhost:8080");
        config.setUsername("local_user");
        config.setPassword("local_password");
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