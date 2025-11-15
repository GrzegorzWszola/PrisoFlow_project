package com.example.Router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Controllers.UserController;
import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * The {@code NonDbRoutes} class defines all HTTP routes not related to
 * database operations within the main API router.
 *
 * <p>It registers endpoints under the <code>/api/db</code> path and delegates
 * request handling to the {@link com.example.Controllers.NonDbController}.
 *
 * <p>Currently available routes:
 * <ul>
 *   <li><b>GET /api/user/hello</b> — Checks the connection status.</li>
 * </ul>
 *
 * @see com.example.Controllers.DbController
 */
public class UserRoutes {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseRoutes.class);

     /**
     * Registers all routes related to non database operations
     * within the Javalin router context.
     *
     * <p>This method should be invoked during the main router configuration,
     * typically in {@code Router.configure()}.
     *
     * @see com.example.Router.Router
     */
    public static void register() {
        path("/user", () -> {
            logger.info("Route to non databse built: api/user");
            get("/hello", UserController::helloMsg);
            post("/login", UserController::login);
            get("/allUsers", UserController::getAllUsers);
            post("/addUser", UserController::addUser);
            delete("/deleteUser/{userId}", UserController::deleteUser);
            post("/editUser/{userId}", UserController::editUser);
        });
    }
}
