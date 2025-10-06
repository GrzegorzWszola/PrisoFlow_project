package com.example.Router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;

public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    public static void configure(Javalin app) {
        app.routes(() -> {
            logger.info("Api route build: /api");
            path("/api", () -> {
                // Database requests
                DatabaseRoutes.register();
                NonDbRoutes.register();
            });
        });
    }
}
