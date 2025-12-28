package com.example.Router;

import com.example.Controllers.PrisonController;
import static io.javalin.apibuilder.ApiBuilder.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrisonRoutes {
    private static final Logger logger = LoggerFactory.getLogger(PrisonRoutes.class);

    public static void register() {
        path("/prison", () -> {
            logger.info("Route to databse built: api/prison");
            PrisonController prisonController = new PrisonController();
            get("/getAllPrisons", prisonController::getAllPrisons);
            delete("/deletePrison/{prisonId}", prisonController::deletePrison);
            post("/addPrison", prisonController::addPrison);
            post("/editPrison/{prisonId}", prisonController::editPrison);
        });
    }
}