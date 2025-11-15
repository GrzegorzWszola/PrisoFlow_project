package com.example.Router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.javalin.Javalin;
import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * The {@code Router} class is responsible for configuring and registering
 * all API routes in the Javalin application.
 *
 * <p>This class acts as the main entry point for route setup and delegates
 * route registration to specific sub-route classes such as
 * {@link com.example.Router.DatabaseRoutes} and {@link com.example.Router.NonDbRoutes}.
 *
 * <p>Base API path: <code>/api</code>
 * @see com.example.Router.DatabaseRoutes
 * @see com.example.Router.NonDbRoutes
 */
public class Router {
    private static final Logger logger = LoggerFactory.getLogger(Router.class);

    /**
     * Configures all application routes and attaches them to the provided
     * Javalin instance.
     *
     * <p>This method registers the base <code>/api</code> path and delegates
     * sub-route registration to the {@link DatabaseRoutes} and
     * {@link NonDbRoutes} classes.
     *
     * @param app the currently running Javalin server instance
     */
    public static void configure(Javalin app) {
        app.routes(() -> {
            logger.info("Api route build: /api");
            path("/api", () -> {
                // Database requests
                DatabaseRoutes.register();
                UserRoutes.register();
                AdminRoutes.register();
                PrisonRoutes.register();
            });
        });
    }
}
