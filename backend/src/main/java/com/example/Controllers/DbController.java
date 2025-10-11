package com.example.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;
import com.example.Objects.PrisonRecord;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public static void createTable(Context ctx) {
        logger.info("Redirected to checkHealth: api/db/createTable");
        String sql = """
            CREATE TABLE prisons (
                prison_id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                location VARCHAR(150),
                capacity INT CHECK (capacity >= 0),
                security_level VARCHAR(50),
                warden_name VARCHAR(100),
                contact_phone VARCHAR(20),
                contact_email VARCHAR(100),
                established_year INT CHECK (established_year >= 1800 AND established_year <= EXTRACT(YEAR FROM CURRENT_DATE)),
                notes TEXT
            );
        """;

        try (Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement()) {

            logger.info("Database connection established — creating table...");
            stmt.executeUpdate(sql);

            ctx.status(200).result("Table 'kierowcy' created successfully!");
            logger.info("Table 'kierowcy' created or already exists.");

        } catch (SQLException e) {
            logger.error("Error while creating table: ", e);
            ctx.status(500).result("Database error: " + e.getMessage());
        }
    }

    public static void testCreatePrRecord(Context ctx) {
        logger.info("Redirected to checkHealth: api/db/testCreatePrRecord");

        String insertSQL = """
            INSERT INTO prisons
            (name, location, capacity, security_level, warden_name, contact_phone, contact_email, established_year, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ds.getConnection();
            PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            // Przykładowe dane — można potem pobierać z requestu (ctx.formParam / ctx.bodyAsClass)
            stmt.setString(1, "Central Prison Wrocław");
            stmt.setString(2, "Wrocław, ul. Więzienna 12");
            stmt.setInt(3, 1200);
            stmt.setString(4, "High");
            stmt.setString(5, "Jan Kowalski");
            stmt.setString(6, "+48 123 456 789");
            stmt.setString(7, "kontakt@wiezienie.pl");
            stmt.setInt(8, 1954);
            stmt.setString(9, "Największe więzienie w regionie.");

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ctx.status(201).result("Prison record created successfully!");
                logger.info("Inserted new prison record into DB");
            } else {
                ctx.status(500).result("No record inserted!");
                logger.warn("Insert statement executed but no rows affected");
            }

        } catch (SQLException e) {
            logger.error("Couldn't insert record into database: ", e);
            ctx.status(500).result("Database error: " + e.getMessage());
        }
    }

    public static void testReadPrRecord(Context ctx) {
        try (Connection conn = ds.getConnection()) {
        String sql = "SELECT * FROM prisons ORDER BY prison_id";
        List<PrisonRecord> prisons = new ArrayList<>();
        
            try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    PrisonRecord prison = new PrisonRecord();
                    prison.setPrisonId(rs.getInt("prison_id"));
                    prison.setName(rs.getString("name"));
                    prison.setLocation(rs.getString("location"));
                    
                    // Obsługa NULL dla Integer
                    int capacity = rs.getInt("capacity");
                    prison.setCapacity(rs.wasNull() ? null : capacity);
                    
                    prison.setSecurityLevel(rs.getString("security_level"));
                    prison.setWardenName(rs.getString("warden_name"));
                    prison.setContactPhone(rs.getString("contact_phone"));
                    prison.setContactEmail(rs.getString("contact_email"));
                    
                    int year = rs.getInt("established_year");
                    prison.setEstablishedYear(rs.wasNull() ? null : year);
                    
                    prison.setNotes(rs.getString("notes"));
                    
                    prisons.add(prison);
                }

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(prisons);

                ctx.status(200)
                    .contentType("application/json")
                    .result(json);            
                logger.info("Successfully retrieved {} prison records", prisons.size());
            }  
        } catch(Exception e) {
            logger.error("Error reading prison records: ", e);
            ctx.status(500)
            .contentType("application/json")
            .result("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    public static void testDropRecord(Context ctx) {
        logger.info("Deleting prison record: api/db/testDropRecord");
        
        try (Connection conn = ds.getConnection()) {
            // Pobierz ID z parametru query lub path
            String idParam = "1"; // lub ctx.pathParam("id")
            
            if (idParam == null || idParam.isEmpty()) {
                ctx.status(400)
                .contentType("application/json")
                .result("{\"error\": \"Prison ID is required\"}");
                return;
            }
            
            int prisonId = Integer.parseInt(idParam);
            String sql = "DELETE FROM prisons WHERE prison_id = ?";
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, prisonId);
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    ctx.status(200)
                    .contentType("application/json")
                    .result("{\"message\": \"Prison record deleted successfully\", \"id\": " + prisonId + "}");
                    logger.info("Deleted prison record with ID: {}", prisonId);
                } else {
                    ctx.status(404)
                    .contentType("application/json")
                    .result("{\"error\": \"Prison record not found\", \"id\": " + prisonId + "}");
                }
            }
            
        } catch (NumberFormatException e) {
            logger.error("Invalid prison ID format: ", e);
            ctx.status(400)
            .contentType("application/json")
            .result("{\"error\": \"Invalid prison ID format\"}");
        } catch(Exception e) {
            logger.error("Error deleting prison record: ", e);
            ctx.status(500)
            .contentType("application/json")
            .result("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }

    public static void testDropTable(Context ctx) {
        logger.info("Dropping prisons table: api/db/testDropTable");
        
        try (Connection conn = ds.getConnection()) {
            String sql = "DROP TABLE IF EXISTS prisons CASCADE";
            
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                
                ctx.status(200)
                .contentType("application/json")
                .result("{\"message\": \"Prisons table dropped successfully\"}");
                
                logger.info("Prisons table dropped successfully");
            }
            
        } catch(Exception e) {
            logger.error("Error dropping prisons table: ", e);
            ctx.status(500)
            .contentType("application/json")
            .result("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
}