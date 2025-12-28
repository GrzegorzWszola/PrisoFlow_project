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
            AdminController adminController = new AdminController();
            get("/test", adminController::testAdmin);
            path("/backup", () -> {
                get("/list", adminController::getAllBackups);
                post("/create", adminController::createBackup);
                post("/restore/{filename}", adminController::restoreBackup);
                post("/remove/{filename}", adminController::removeBackup);
            });
        });
    }
}
