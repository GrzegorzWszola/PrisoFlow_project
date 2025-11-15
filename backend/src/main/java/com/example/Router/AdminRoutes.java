package com.example.Router;

import com.example.Controllers.AdminController;
import static io.javalin.apibuilder.ApiBuilder.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AdminRoutes {
    private static final Logger logger = LoggerFactory.getLogger(AdminRoutes.class);

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
        path("/admin", () -> {
            logger.info("Route to admin built: api/admin");
            get("/test", AdminController::testAdmin);
            path("/backup", () -> {
                get("/list", AdminController::getAllBackups);
                post("/create", AdminController::createBackup);
                post("/restore/{filename}", AdminController::restoreBackup);
                post("/remove/{filename}", AdminController::removeBackup);
            });
        });
    }
}
