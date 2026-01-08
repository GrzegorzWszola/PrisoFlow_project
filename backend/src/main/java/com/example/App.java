package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Controllers.AdminController;
import com.example.Controllers.DbController;
import com.example.Controllers.PrisonController;
import com.example.Controllers.UserController;
import com.example.Router.Router;

import io.github.cdimascio.dotenv.Dotenv;
import io.javalin.Javalin;

public class App {
    // Create logger
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        // Load .env files
        Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();
        
        // Get it to string
        String allowedOriginsEnv = dotenv.get("ALLOWED_ORIGINS");
        String[] allowedOrigins;

        // Check for blanks and split the values into list
        if (allowedOriginsEnv != null && !allowedOriginsEnv.isBlank()) {
            allowedOrigins = allowedOriginsEnv.split(",");
            logger.info("Succesfully opened and split the .env file");
        } else {
            allowedOrigins = new String[]{"http://localhost:3000", "http://localhost:5173"};
            logger.info("Problem with .env file setting cors links to http://localhost:3000");
        }

        // Cast strings into final
        final String[] originsForLambda = allowedOrigins;

        // Create server with avaible cors
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> {
                    for (String origin : originsForLambda) {
                        it.allowHost(origin);
                    }
                    it.allowCredentials = true;
                });
            });
        // Start listening on "0.0.0.0" port 7000
        });

        // Initialize router to route the traffic
        Router.configure(app);

        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "7000"));
        app.start("0.0.0.0", port);
    }

    public static Javalin createApp() {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableCors(cors -> {
                cors.add(it -> it.anyHost());
            });
        });

        // Zarejestruj wszystkie routy
        DbController dbController = new DbController();
        UserController userController = new UserController();
        PrisonController prisonController = new PrisonController();
        AdminController adminController = new AdminController();

        app.get("/api/hello", userController::helloMsg);
        app.post("/api/login", userController::login);
        app.get("/api/db/health", dbController::checkHealth);
        app.get("/api/db/dashboard", dbController::dashboard);
        app.get("/api/prisons", prisonController::getAllPrisons);
        app.post("/api/prisons", prisonController::addPrison);
        app.put("/api/prisons/{prisonId}", prisonController::editPrison);
        app.delete("/api/prisons/{prisonId}", prisonController::deletePrison);
        app.get("/api/users", userController::getAllUsers);
        app.post("/api/users", userController::addUser);
        app.put("/api/users/{userId}", userController::editUser);
        app.delete("/api/users/{userId}", userController::deleteUser);
        app.get("/api/admin/test", adminController::testAdmin);
        app.get("/api/admin/backups", adminController::getAllBackups);

        return app;
    }
}