package com.example.Controllers;

import java.sql.Connection;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;

import io.javalin.http.Context;

public class DbController{
    // Create data source
    private static final DataSource ds = DatabaseConfig.getDataSource();
    private static final Logger logger = LoggerFactory.getLogger(DbController.class);

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