package com.example.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;
import com.example.Objects.Prison;

import io.javalin.http.Context;

public class PrisonController {
    private static final DataSource ds = DatabaseConfig.getDataSource();
    private static final Logger logger = LoggerFactory.getLogger(DbController.class);
    
    public static void getAllPrisons(Context ctx) {
        try (Connection conn = ds.getConnection()) {
            String sql = "SELECT * FROM prisons";
            List<Prison> prisonList = new ArrayList<Prison>();

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
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

                if (!prisonList.isEmpty() || prisonList == null){
                    ctx.status(200).json(prisonList);
                } else {
                    ctx.status(404).json(Map.of("error", "Error with prisons table"));
                }
            }
        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public static void deletePrison(Context ctx) {
        try(Connection conn = ds.getConnection()){
            int prisonId = Integer.parseInt(ctx.pathParam("prisonId"));
            String sql = "DELETE FROM prisons WHERE prison_id=?";

            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, prisonId);
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    ctx.status(201).json(Map.of("message", "Prison delted successfully"));
                } else {
                    ctx.status(400).json(Map.of("message", "Failed to delete prison"));
                }
            }

        }  catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public static void addPrison(Context ctx) {
        try(Connection conn = ds.getConnection()){
            Prison prison = ctx.bodyAsClass(Prison.class);
            String sql = "INSERT INTO prisons(name, location, capacity, security_level, opening_date, number_of_cells, is_active) VALUES (?, ?, ?, ?, CURRENT_DATE, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, prison.getName());
                stmt.setString(2, prison.getLocation());
                stmt.setInt(3, prison.getCapacity());
                stmt.setString(4, prison.getSecurityLevel());
                stmt.setInt(5, prison.getNumOfCells());
                stmt.setBoolean(6, prison.getIsActive());
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    ctx.status(201).json(Map.of("message", "Prison added successfully"));
                } else {
                    ctx.status(400).json(Map.of("message", "Failed to add prison"));
                }
            }

        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public static void editPrison(Context ctx) {
        try(Connection conn = ds.getConnection()){
            Prison prison = ctx.bodyAsClass(Prison.class);
            int prisonId = Integer.parseInt(ctx.pathParam("prisonId"));
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

            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, prison.getName());
                stmt.setString(2, prison.getLocation());
                stmt.setInt(3, prison.getCapacity());
                stmt.setString(4, prison.getSecurityLevel());
                stmt.setInt(5, prison.getNumOfCells());
                stmt.setBoolean(6, prison.getIsActive());
                stmt.setInt(7, prisonId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    ctx.status(201).json(Map.of("message", "Prison edited successfully"));
                } else {
                    ctx.status(400).json(Map.of("message", "Failed to edit prison"));
                }
            }
        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    } 
}
