package com.example.Router;

import com.example.Controllers.DbController;
import static io.javalin.apibuilder.ApiBuilder.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@code DatabaseRoutes} class defines all HTTP routes related to
 * database operations within the main API router.
 *
 * <p>It registers endpoints under the <code>/api/db</code> path and delegates
 * request handling to the {@link com.example.Controllers.DbController}.
 *
 * <p>Currently available routes:
 * <ul>
 *   <li><b>GET /api/db/health</b> — Checks the database connection status.</li>
 *   <!-- <li><b>GET /api/db/stats</b> — Returns database statistics.</li> -->
 * </ul>
 *
 * @see com.example.Controllers.DbController
 */
public class DatabaseRoutes {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseRoutes.class);

    /**
     * Registers all routes related to database operations
     * within the Javalin router context.
     *
     * <p>This method should be invoked during the main router configuration,
     * typically in {@code Router.configure()}.
     *
     * @see com.example.Router.Router
     */
    public static void register() {
        path("/db", () -> {
            logger.info("Route to databse built: api/db");
            DbController dbController = new DbController();
            get("/health", dbController::checkHealth);
            get("/prisonInfo", dbController::prisonInfo);
            get("/dashboard", dbController::dashboard);
        });
    }
}
