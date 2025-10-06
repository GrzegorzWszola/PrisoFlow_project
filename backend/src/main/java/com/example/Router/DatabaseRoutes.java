package com.example.Router;

import com.example.Controllers.DbController;
import static io.javalin.apibuilder.ApiBuilder.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseRoutes {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseRoutes.class);

    public static void register() {
        path("/db", () -> {
            logger.info("Route to databse built: api/db");
            get("/health", DbController::checkHealth);
            // get("/stats", DbController::getStats);
        });
    }
}
