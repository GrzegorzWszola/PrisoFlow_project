package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        // Create router to route the traffic
        Router.configure(app);

        app.start("0.0.0.0", 7000);
    }
}