package com.example.Router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Controllers.NonDbController;
import static io.javalin.apibuilder.ApiBuilder.*;

public class NonDbRoutes {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseRoutes.class);

    public static void register() {
        path("/user", () -> {
            logger.info("Route to non databse built: api/user");
            get("/hello", NonDbController::helloMsg);
            // get("/stats", DbController::getStats);
        });
    }
}
