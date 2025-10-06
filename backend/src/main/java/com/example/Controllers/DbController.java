package com.example.Controllers;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;

import io.javalin.http.Context;

/**
 * The {@code DbController} class handles API endpoints that interact
 * directly with the database.
 *
 * <p>It provides methods for health checks, data retrieval, and other
 * database-related operations, ensuring backend connectivity and proper
 * error handling.
 *
 * <p>All database connections are acquired through the shared
 * {@link DatabaseConfig#getDataSource()} instance.
 * 
 * @see com.example.Router.DatabaseRoutes
 */
public class DbController{
    // Create data source
    private static final DataSource ds = DatabaseConfig.getDataSource();
    private static final Logger logger = LoggerFactory.getLogger(DbController.class);

    /**
     * Checks the health of the database connection.
     *
     * <p>Attempts to acquire a connection from the configured data source.
     * If successful, it returns <b>200 OK</b> with a confirmation message.
     * If the connection fails, it logs the error and responds with
     * <b>500 Internal Server Error</b>.
     *
     * @param ctx the {@link Context} object representing the HTTP request
     *            and response in Javalin
     *
     * @throws RuntimeException if the database connection cannot be established
     */
    public static void checkHealth(Context ctx) {
        logger.info("Redirected to checkHealth: api/db/health");
        try (Connection conn = ds.getConnection()) {
            logger.info("Database connection healthy");
            ctx.status(200).result("Database connection is healthy!");
        } catch(Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }
}