package com.example.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;
import com.example.Objects.Prison;

import io.javalin.http.Context;

/**
 * The {@code DbController} class handles API endpoints that interact
 * directly with the database.
 *
 * <p>It provides methods for health checks, data retrieval, and other
 * database-related operations, ensuring backend connectivity and proper
 * error handling.
 *
 * <p>All database connections are acquired through the injected
 * {@link DataSource} instance.
 * 
 * @see com.example.Router.DatabaseRoutes
 */
public class DbController {
    private final DataSource dataSource;
    private final Logger logger;

    /**
     * Constructor with dependency injection for testing.
     *
     * @param dataSource the data source to use for database connections
     */
    public DbController(DataSource dataSource) {
        this.dataSource = dataSource;
        this.logger = LoggerFactory.getLogger(DbController.class);
    }

    /**
     * Default constructor for production use.
     * Uses the default DataSource from DatabaseConfig.
     */
    public DbController() {
        this(DatabaseConfig.getDataSource());
    }

    /**
     * Checks the health of the database connection.
     *
     * <p>Attempts to acquire a connection from the configured data source.
     * If successful, it returns <b>200 OK</b> with a confirmation message.
     * If the connection fails, it logs the error and responds with
     * <b>500 Internal Server Error</b>.
     *
     * @param ctx the {@link Context} object representing the HTTP request
     *            and response in Javalin
     *
     * @throws RuntimeException if the database connection cannot be established
     */
    public void checkHealth(Context ctx) {
        logger.info("Redirected to checkHealth: api/db/health");
        try (Connection conn = dataSource.getConnection()) {
            logger.info("Database connection healthy");
            ctx.status(200).result("Database connection is healthy!");
        } catch(Exception e) {
            logger.error("Couldn't connect to database: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves all prisons from the database.
     *
     * @param ctx the {@link Context} object
     * @return a list of {@link Prison} objects, or null if an error occurs
     */
    public List<Prison> getPrisonsFromDb(Context ctx) {
        try (Connection conn = dataSource.getConnection()) {
            List<Prison> prisonList = new ArrayList<>();
            String sql = "SELECT * FROM prisons";
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

                return prisonList;
            } 
        } catch (Exception e) {
            logger.error("Couldn't connect to database: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
            return null;
        }
    }

    /**
     * Endpoint to retrieve prison information.
     *
     * @param ctx the {@link Context} object
     */
    public void prisonInfo(Context ctx) {
        logger.info("Redirected to prisonInfo: api/db/prisonInfo");
        
        List<Prison> prisonList = getPrisonsFromDb(ctx);

        if (prisonList != null && !prisonList.isEmpty()) {
            ctx.status(200).json(prisonList);
        } else {
            ctx.status(404).json(Map.of("error", "Error with prisons table"));
        }
    }

    /**
     * Endpoint to retrieve dashboard data including prisons, visits, and incidents.
     *
     * @param ctx the {@link Context} object
     */
    public void dashboard(Context ctx) {
        logger.info("Redirected to dashboard: api/db/dashboard");
        List<Map<String, Object>> prisonsData = new ArrayList<>();
        List<Map<String, Object>> visitsData = new ArrayList<>();
        List<Map<String, Object>> incidentData = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            
            String sql = """
                SELECT   
                    p.prison_id,
                    p.prison_name,
                    p.location,
                    p.capacity,
                    p.security_level,
                    p.current_inmates,
                    p.occupancy_percentage    
                FROM public.view_prison_occupancy p
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {    
                while(rs.next()) {
                    Map<String, Object> prisonStats = new HashMap<>();
                    prisonStats.put("id", rs.getInt("prison_id"));
                    prisonStats.put("name", rs.getString("prison_name"));
                    prisonStats.put("location", rs.getString("location"));
                    prisonStats.put("capacity", rs.getInt("capacity"));
                    prisonStats.put("securityLevel", rs.getString("security_level"));
                    prisonStats.put("currentInmates", rs.getInt("current_inmates"));
                    prisonStats.put("occupancyPercentage", rs.getDouble("occupancy_percentage"));
                    
                    prisonsData.add(prisonStats);
                }               
            }

            sql = """
                SELECT 
                    c.first_name, 
                    c.last_name, 
                    p.name AS prison_name, 
                    pv.visitor_first_name, 
                    pv.visitor_last_name, 
                    pv.relationship,
                    pv.visit_datetime
                FROM prison_visits pv
                LEFT JOIN criminals c ON pv.criminal_id = c.criminal_id
                LEFT JOIN prisons p ON pv.prison_id = p.prison_id
                WHERE pv.is_approved = TRUE
                ORDER BY pv.visit_datetime
                LIMIT 3
            """;

            try(PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Map<String, Object> visitStats = new HashMap<>();
                    visitStats.put("criminal_first_name", rs.getString("first_name"));
                    visitStats.put("criminal_last_name", rs.getString("last_name"));
                    visitStats.put("prison_name", rs.getString("prison_name"));
                    visitStats.put("visitor_first_name", rs.getString("visitor_first_name"));
                    visitStats.put("visitor_last_name", rs.getString("visitor_last_name"));
                    visitStats.put("relationship", rs.getString("relationship"));
                    java.sql.Timestamp tsp = rs.getTimestamp("visit_datetime");
                    if (tsp != null) {
                        visitStats.put("visit_datetime", tsp);
                    }

                    visitsData.add(visitStats);
                }
            }

            sql = """
                SELECT
                    incident_id,
                    incident_datetime,
                    prison_name,
                    incident_type,
                    severity,
                    criminal_involved,
                    officer_involved,
                    description
                FROM view_recent_incidents
            """;

            try(PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
                while(rs.next()) {
                    Map<String, Object> incidentStats = new HashMap<>();
                    incidentStats.put("incident_id", rs.getInt("incident_id"));
                    incidentStats.put("prison_name", rs.getString("prison_name"));
                    incidentStats.put("incident_type", rs.getString("incident_type"));
                    incidentStats.put("severity", rs.getString("severity"));
                    incidentStats.put("criminal_involved", rs.getString("criminal_involved"));
                    incidentStats.put("officer_involved", rs.getString("officer_involved"));
                    
                    java.sql.Timestamp tsp = rs.getTimestamp("incident_datetime");
                    if (tsp != null) {
                        incidentStats.put("incident_datetime", tsp);
                    }
                    
                    incidentStats.put("description", rs.getString("description"));

                    incidentData.add(incidentStats);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("prisons", prisonsData);
            response.put("visits", visitsData);
            response.put("incidents", incidentData);
            ctx.status(200).json(response);
        } catch (SQLException e) {
            logger.error("Database error in dashboard: ", e);
            ctx.status(500).json(Map.of("error", "Database error", "message", e.getMessage()));
        }
    }
}