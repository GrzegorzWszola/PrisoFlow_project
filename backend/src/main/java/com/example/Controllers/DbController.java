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
 * <p>All database connections are acquired through the shared
 * {@link DatabaseConfig#getDataSource()} instance.
 * 
 * @see com.example.Router.DatabaseRoutes
 */
public class DbController{
    // Create data source
    private static final DataSource ds = DatabaseConfig.getDataSource();
    private static final Logger logger = LoggerFactory.getLogger(DbController.class);

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
    public static void checkHealth(Context ctx) {
        logger.info("Redirected to checkHealth: api/db/health");
        try (Connection conn = ds.getConnection()) {
            logger.info("Database connection healthy");
            ctx.status(200).result("Database connection is healthy!");
        } catch(Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public static List<Prison> getPrisonsFromDb(Context ctx){
        try (Connection conn = ds.getConnection()) {
            List<Prison> prisonList = new ArrayList<Prison>();
            String sql = "SELECT * FROM prisons";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                ResultSet rs = stmt.executeQuery();

                while(rs.next()){
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
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
            return null;
        }
    }

    public static void prisonInfo(Context ctx) {
        logger.info("Redirected to prisonInfo: api/db/prisonInfo");
        
        List<Prison> prisonList = getPrisonsFromDb(ctx);

        if (!prisonList.isEmpty() || prisonList == null){
            ctx.status(200).json(prisonList);
        } else {
            ctx.status(404).json(Map.of("error", "Error with prisons table"));
        }
    }

    public static void dashboard(Context ctx) {
        logger.info("Redirected to dashboard: api/db/dashboard");
        List<Map<String, Object>> prisonsData = new ArrayList<>();
        List<Map<String, Object>> visitsData = new ArrayList<>();
        List<Map<String, Object>> incidentData = new ArrayList<>();

        try (Connection conn = ds.getConnection()) {
            String sql = """
                SELECT 
                    p.prison_id,
                    p.name,
                    p.location,
                    p.capacity,
                    p.security_level,
                    COUNT(c.criminal_id) as current_inmates,
                    CASE 
                        WHEN p.capacity > 0 THEN ROUND((COUNT(c.criminal_id)::numeric / p.capacity::numeric) * 100, 2)
                        ELSE 0
                    END as occupancy_percentage
                FROM prisons p
                LEFT JOIN criminals c ON c.prison_id = p.prison_id
                WHERE p.is_active = true
                GROUP BY p.prison_id, p.name, p.location, p.capacity, p.security_level
                ORDER BY occupancy_percentage DESC
            """;
            
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {    
                while(rs.next()) {
                    Map<String, Object> prisonStats = new HashMap<>();
                    prisonStats.put("id", rs.getInt("prison_id"));
                    prisonStats.put("name", rs.getString("name"));
                    prisonStats.put("location", rs.getString("location"));
                    prisonStats.put("capacity", rs.getInt("capacity"));
                    prisonStats.put("securityLevel", rs.getString("security_level"));
                    prisonStats.put("currentInmates", rs.getInt("current_inmates"));
                    prisonStats.put("occupancyPercentage", rs.getDouble("occupancy_percentage"));
                    
                    prisonsData.add(prisonStats);
                }               
            }

            sql = 
                """
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
                LIMIT 3;
                """;

            try(PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
                while(rs.next()){
                    Map<String, Object> visitStats = new HashMap<>();
                    visitStats.put("criminal_first_name", rs.getString("first_name"));
                    visitStats.put("criminal_last_name", rs.getString("last_name"));
                    visitStats.put("prison_name", rs.getString("prison_name"));
                    visitStats.put("visitor_first_name", rs.getString("visitor_first_name"));
                    visitStats.put("visitor_last_name", rs.getString("visitor_last_name"));
                    visitStats.put("relationship", rs.getString("relationship"));
                    java.sql.Timestamp tsp = rs.getTimestamp("visit_datetime");
                    if (tsp != null){
                        visitStats.put("visit_datetime", tsp);
                    }

                    visitsData.add(visitStats);
                }
            }

            sql = 
                """
                SELECT
                    pi.incident_id,
                    p.name AS prison_name, 
                    c.first_name AS criminal_first_name,
                    c.last_name AS criminal_last_name,
                    o.first_name AS officer_first_name,
                    o.last_name AS officer_last_name,
                    pi.incident_datetime,
                    pi.incident_type,
                    pi.description,
                    pi.severity
                FROM prison_incidents pi
                LEFT JOIN criminals c ON c.criminal_id = pi.criminal_id
                LEFT JOIN prisons p ON p.prison_id = pi.prison_id
                LEFT JOIN officers o ON o.officer_id = pi.officer_id
                ORDER BY pi.incident_datetime
                LIMIT 3
                """;

            try(PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {
                while(rs.next()){
                    Map<String, Object> incidentStats = new HashMap<>();
                    incidentStats.put("incident_id", rs.getInt("incident_id"));
                    incidentStats.put("prison_name", rs.getString("prison_name"));
                    incidentStats.put("criminal_first_name", rs.getString("criminal_first_name"));
                    incidentStats.put("criminal_last_name", rs.getString("criminal_last_name"));
                    incidentStats.put("officer_first_name", rs.getString("officer_first_name"));
                    incidentStats.put("officer_last_name", rs.getString("officer_last_name"));
                    java.sql.Timestamp tsp = rs.getTimestamp("incident_datetime");
                    if (tsp != null){
                        incidentStats.put("incident_datetime", tsp);
                    }
                    incidentStats.put("incident_type", rs.getString("incident_type"));
                    incidentStats.put("descryption", rs.getString("description"));
                    incidentStats.put("severity", rs.getString("severity"));

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
            return;
        }


    }
}