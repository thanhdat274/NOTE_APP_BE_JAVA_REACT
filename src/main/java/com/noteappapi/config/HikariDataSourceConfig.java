package com.noteappapi.config;
import com.noteappapi.model.HikariPool;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@Configuration
public class HikariDataSourceConfig {
    private final HikariPool hikariPool;

    public HikariDataSourceConfig(HikariPool hikariPool) {
        this.hikariPool = hikariPool;
    }

    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(hikariPool.getDriverClassName());
        config.setJdbcUrl(hikariPool.getJdbcUrl());
        config.setUsername(hikariPool.getUsername());
        config.setPassword(hikariPool.getPassword());
        config.setConnectionTimeout(hikariPool.getConnectionTimeout());
        config.setIdleTimeout(hikariPool.getIdleTimeout());
        config.setMaxLifetime(hikariPool.getMaxLifetime());
        config.setMinimumIdle(hikariPool.getMinimumIdle());
        config.setMaximumPoolSize(hikariPool.getMaximumPoolSize());
        // Bật tính năng cache PreparedStatement để tối ưu hóa hiệu suất truy vấn.
        config.addDataSourceProperty("cachePrepStmts", "true");
        // Đặt số lượng tối đa của các PreparedStatement được cache trong bộ nhớ.
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        // Đặt kích thước tối đa cho các truy vấn SQL có thể được cache bởi PreparedStatement.
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        log.info("Configuration hikari pool: {}", config);
        return new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource().getConnection();
    }
}
