package com.example.Controllers;

import javax.sql.DataSource;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;

import io.javalin.http.Context;

/**
 * The {@code NonDbController} class handles API endpoints that do not
 * require direct interaction with the database.
 *
 * <p>This controller provides lightweight responses that test backend
 * connectivity or return static messages. It can also be used to verify
 * the application's overall availability from the frontend.
 * @see com.example.Router.NonDbRoutes
 */
public class NonDbController {
    private static final DataSource ds = DatabaseConfig.getDataSource();
    private static final Logger logger = LoggerFactory.getLogger(DbController.class);

    /**
     * Handles a simple health-check or test request.
     *
     * <p>Attempts to acquire a database connection to confirm that the backend
     * is operational. If successful, it returns a <b>200 OK</b> status with
     * the message <i>"Hello from backend"</i>. If the connection fails,
     * it responds with a <b>500 Internal Server Error</b> and logs the cause.
     *
     * @param ctx the {@link Context} object representing the HTTP request
     *            and response in Javalin
     *
     * @throws RuntimeException if the database connection cannot be established
     */
    public static void helloMsg(Context ctx) {
        try (Connection conn = ds.getConnection()){
            ctx.status(200).result("Hello from backend");
        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }
}
