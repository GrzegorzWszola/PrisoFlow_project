package com.example.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;
import com.example.Objects.Prison;

import io.javalin.http.Context;

/**
 * The {@code PrisonController} class handles CRUD operations for prisons.
 *
 * <p>It provides methods for retrieving, adding, updating, and deleting
 * prison records from the database.
 *
 * @see com.example.Objects.Prison
 */
public class PrisonController {
    private final DataSource dataSource;
    private final Logger logger;

    /**
     * Constructor with dependency injection for testing.
     *
     * @param dataSource the data source to use for database connections
     */
    public PrisonController(DataSource dataSource) {
        this.dataSource = dataSource;
        this.logger = LoggerFactory.getLogger(PrisonController.class);
    }

    /**
     * Default constructor for production use.
     * Uses the default DataSource from DatabaseConfig.
     */
    public PrisonController() {
        this(DatabaseConfig.getDataSource());
    }

    /**
     * Retrieves all prisons from the database.
     *
     * @param ctx the {@link Context} object representing the HTTP request and response
     */
    public void getAllPrisons(Context ctx) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM prisons";
            List<Prison> prisonList = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while(rs.next()) {
                    Prison prison = new Prison();

                    prison.setId(rs.getInt("prison_id"));
                    prison.setName(rs.getString("name"));
                    prison.setLocation(rs.getString("location"));
                    prison.setCapacity(rs.getInt("capacity"));
                    prison.setSecurityLevel(rs.getString("security_level"));
                    java.sql.Date sqlDate = rs.getDate("opening_date");
                    if (sqlDate != null) {
                        prison.setDate(sqlDate.toLocalDate());
                    }
                    prison.setNumOfCells(rs.getInt("number_of_cells"));
                    prison.setIsActive(rs.getBoolean("is_active"));

                    prisonList.add(prison);
                }

                if (!prisonList.isEmpty()) {
                    ctx.status(200).json(prisonList);
                } else {
                    ctx.status(404).json(Map.of("error", "No prisons found"));
                }
            }
        } catch (Exception e) {
            logger.error("Couldn't connect to database: ", e);
            ctx.status(500).json(Map.of(
                "error", "Database connection failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Deletes a prison by ID.
     *
     * @param ctx the {@link Context} object containing the prison ID as path parameter
     */
    public void deletePrison(Context ctx) {
        try(Connection conn = dataSource.getConnection()) {
            int prisonId = Integer.parseInt(ctx.pathParam("prisonId"));
            String sql = "DELETE FROM prisons WHERE prison_id=?";

            try(PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, prisonId);
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    ctx.status(200).json(Map.of("message", "Prison deleted successfully"));
                } else {
                    ctx.status(404).json(Map.of("message", "Prison not found"));
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid prison ID format: ", e);
            ctx.status(400).json(Map.of(
                "error", "Invalid prison ID",
                "message", "Prison ID must be a number"
            ));
        } catch (Exception e) {
            logger.error("Couldn't connect to database: ", e);
            ctx.status(500).json(Map.of(
                "error", "Database connection failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Adds a new prison to the database.
     *
     * @param ctx the {@link Context} object containing the prison data in request body
     */
    public void addPrison(Context ctx) {
        try(Connection conn = dataSource.getConnection()) {
            Prison prison = ctx.bodyAsClass(Prison.class);
            
            if (prison.getName() == null || prison.getName().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Prison name is required"));
                return;
            }
            
            String sql = "INSERT INTO prisons(name, location, capacity, security_level, opening_date, number_of_cells, is_active) VALUES (?, ?, ?, ?, CURRENT_DATE, ?, ?)";
            
            // DODANO: Statement.RETURN_GENERATED_KEYS
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, prison.getName());
                stmt.setString(2, prison.getLocation());
                stmt.setInt(3, prison.getCapacity());
                stmt.setString(4, prison.getSecurityLevel());
                stmt.setInt(5, prison.getNumOfCells());
                stmt.setBoolean(6, prison.getIsActive());
                
                stmt.executeUpdate();

                // POBIERANIE GENEROWANEGO ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        ctx.status(201).json(Map.of(
                            "message", "Prison added successfully",
                            "prison_id", newId // Tego szuka test
                        ));
                    } else {
                        ctx.status(400).json(Map.of("message", "Failed to add prison"));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error adding prison: ", e);
            ctx.status(500).json(Map.of("error", "Database error", "message", e.getMessage()));
        }
    }

    /**
     * Updates an existing prison.
     *
     * @param ctx the {@link Context} object containing the prison ID and updated data
     */
    public void editPrison(Context ctx) {
        try(Connection conn = dataSource.getConnection()) {
            Prison prison = ctx.bodyAsClass(Prison.class);
            int prisonId = Integer.parseInt(ctx.pathParam("prisonId"));
            
            // Walidacja danych wejściowych
            if (prison.getName() == null || prison.getName().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Prison name is required"));
                return;
            }
            
            String sql = """
            UPDATE prisons SET
            name = ?,
            location = ?,
            capacity = ?,
            security_level = ?,
            number_of_cells = ?,
            is_active = ?
            WHERE prison_id = ?
            """;

            try(PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, prison.getName());
                stmt.setString(2, prison.getLocation());
                stmt.setInt(3, prison.getCapacity());
                stmt.setString(4, prison.getSecurityLevel());
                stmt.setInt(5, prison.getNumOfCells());
                stmt.setBoolean(6, prison.getIsActive());
                stmt.setInt(7, prisonId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    ctx.status(200).json(Map.of("message", "Prison edited successfully"));
                } else {
                    ctx.status(404).json(Map.of("message", "Prison not found"));
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid prison ID format: ", e);
            ctx.status(400).json(Map.of(
                "error", "Invalid prison ID",
                "message", "Prison ID must be a number"
            ));
        } catch (Exception e) {
            logger.error("Couldn't connect to database: ", e);
            ctx.status(500).json(Map.of(
                "error", "Database connection failed",
                "message", e.getMessage()
            ));
        }
    } 
}