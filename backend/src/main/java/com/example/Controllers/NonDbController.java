package com.example.Controllers;

import javax.sql.DataSource;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;

import io.javalin.http.Context;

public class NonDbController {
    private static final DataSource ds = DatabaseConfig.getDataSource();
    private static final Logger logger = LoggerFactory.getLogger(DbController.class);

    public static void helloMsg(Context ctx) {
        try (Connection conn = ds.getConnection()){
            ctx.status(200).result("Hello from backend");
        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }
}
