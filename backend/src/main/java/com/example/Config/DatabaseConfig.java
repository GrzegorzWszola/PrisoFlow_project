package com.example.Config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    
    private static void initialize() {
        HikariConfig config = new HikariConfig();
        
        // Setting up connection to database
        config.setJdbcUrl(System.getenv().getOrDefault(
            "DB_URL", 
            "jdbc:postgresql://localhost:5432/mydb"
        ));
        config.setUsername(System.getenv().getOrDefault("DB_USER", "postgres"));
        config.setPassword(System.getenv().getOrDefault("DB_PASS", "postgres"));
        
        // Connection pool settings
        config.setMaximumPoolSize(10);        
        config.setMinimumIdle(2);             
        config.setConnectionTimeout(30000);    
        config.setIdleTimeout(600000);        
        config.setMaxLifetime(1800000);       
        
        // Performance
        config.setAutoCommit(true);
        config.setConnectionTestQuery("SELECT 1");
        
        dataSource = new HikariDataSource(config);
        logger.info("DataSource initialized with pool size: " + config.getMaximumPoolSize());
    }
    
    public static DataSource getDataSource() {
        if (dataSource == null) {
            initialize();
        }
        return dataSource;
    }
    
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
    
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}