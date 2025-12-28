package com.example.Config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The {@code DatabaseConfig} class manages the database connection
 * configuration and provides a HikariCP connection pool for the application.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Initializing the HikariCP {@link HikariDataSource} with environment-based settings.</li>
 *   <li>Providing access to {@link DataSource} and {@link Connection} objects.</li>
 *   <li>Closing the connection pool when the application shuts down.</li>
 * </ul>
 *
 * <p>Configuration defaults to a local PostgreSQL database if environment
 * variables are not provided:
 * <ul>
 *   <li>DB_URL = jdbc:postgresql://localhost:5432/mydb</li>
 *   <li>DB_USER = postgres</li>
 *   <li>DB_PASS = postgres</li>
 * </ul>
 *
 * <p>The connection pool is preconfigured with:
 * <ul>
 *   <li>Maximum pool size: 10</li>
 *   <li>Minimum idle connections: 2</li>
 *   <li>Connection timeout: 30 seconds</li>
 *   <li>Idle timeout: 10 minutes</li>
 *   <li>Maximum lifetime: 30 minutes</li>
 * </ul>
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    
    /**
     * Initializes the HikariCP {@link HikariDataSource} with settings
     * taken from environment variables or defaults.
     */
    private static void initialize() {
        HikariConfig config = new HikariConfig();
        
        // Setting up connection to database
        config.setJdbcUrl(System.getenv().getOrDefault(
            "DB_URL", 
            "jdbc:postgresql://localhost:5432/prisonflow"
        ));
        String dbUrl = System.getProperty("db.url", 
                      System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/prisonflow"));
        
        String dbUser = System.getProperty("db.user", 
                       System.getenv().getOrDefault("DB_USER", "postgres"));
        
        String dbPass = System.getProperty("db.password", 
                       System.getenv().getOrDefault("DB_PASS", "postgres"));

        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPass);
        
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
    
    /**
     * Returns the shared {@link DataSource} instance.
     * Initializes it if not already created.
     *
     * @return the configured {@link DataSource}
     */
    public static DataSource getDataSource() {
        if (dataSource == null) {
            initialize();
        }
        return dataSource;
    }
    
    /**
     * Returns a new {@link Connection} from the connection pool.
     *
     * @return a {@link Connection} object
     * @throws SQLException if acquiring a connection fails
     */
    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }
    
    /**
     * Closes the HikariCP {@link DataSource} if it has been initialized
     * and is not already closed.
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}