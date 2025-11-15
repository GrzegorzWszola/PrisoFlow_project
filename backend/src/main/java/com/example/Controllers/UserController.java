package com.example.Controllers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.Config.DatabaseConfig;
import com.example.Objects.*;

import io.javalin.http.Context;

/**
 * The {@code NonDbController} class handles API endpoints that do not
 * require direct interaction with the database.
 *
 * <p>This controller provides lightweight responses that test backend
 * connectivity or return static messages. It can also be used to verify
 * the application's overall availability from the frontend.
 * @see com.example.Router.NonDbRoutes
 */
public class UserController {
    private static final DataSource ds = DatabaseConfig.getDataSource();
    private static final Logger logger = LoggerFactory.getLogger(DbController.class);
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static User authenticateUser(Connection conn, String username, String password) 
            throws SQLException {
        String sql = "SELECT id, username, email, password, role FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                
                // Sprawdź hasło
                // BCrypt.checkpw(password, hashedPassword) pozniej do kryptowania
                if (BCrypt.checkpw(password, hashedPassword)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    
                    return user;
                }
            }
        }
        return null;
    }

    private static String generateToken(User user) {
        long expirationTime = 86400000; // 24 godziny w milisekundach
        
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * Handles a simple health-check or test request.
     *
     * <p>Attempts to acquire a database connection to confirm that the backend
     * is operational. If successful, it returns a <b>200 OK</b> status with
     * the message <i>"Hello from backend"</i>. If the connection fails,
     * it responds with a <b>500 Internal Server Error</b> and logs the cause.
     *
     * @param ctx the {@link Context} object representing the HTTP request
     *            and response in Javalin
     *
     * @throws RuntimeException if the database connection cannot be established
     */
    public static void helloMsg(Context ctx) {
        try (Connection conn = ds.getConnection()){
            ctx.status(200).result("Hello from backend");
        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Handles a login check.
     *
     * @param ctx the {@link Context} object representing the HTTP request
     *            and response in Javalin
     *
     * @throws RuntimeException if the database connection cannot be established
     */
    public static void login(Context ctx) {
        try (Connection conn = ds.getConnection()) {
                LoginRequest loginData = ctx.bodyAsClass(LoginRequest.class);
                
                String username = loginData.getUsername();
                String password = loginData.getPassword();
                logger.info(username + ", " + password);
                
                // Pobierz użytkownika z bazą wraz z rolą
                User user = authenticateUser(conn, username, password);
                
                if (user != null) {
                    // Zwróć dane użytkownika wraz z rolą
                    LoginResponse response = new LoginResponse(
                        true,
                        "Login successful",
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        generateToken(user)
                    );
                    ctx.status(200).json(response);
                } else {
                    ctx.status(401).json(Map.of("error", "Invalid credentials"));
                }
        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public static void getAllUsers(Context ctx) {
        try (Connection conn = ds.getConnection()){
            String sql = "SELECT * FROM users";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                List<User> usersList = new ArrayList<>();

                while (rs.next()){
                    User user = new User();

                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));

                    usersList.add(user);
                }

                ctx.status(200).json(usersList);    
            }

        } catch (Exception e){
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public static void addUser(Context ctx){
        try(Connection conn = ds.getConnection()){
            User user = ctx.bodyAsClass(User.class);
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            String sql = "INSERT INTO users(username, email, password, role) VALUES (?, ?, ?, ?)";

            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, hashedPassword);
                stmt.setString(4, user.getRole());
                
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    ctx.status(201).json(Map.of("message", "User added successfully"));
                } else {
                    ctx.status(400).json(Map.of("message", "Failed to add user"));
                }                
            }

        } catch (Exception e){
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public static void deleteUser(Context ctx){
        logger.info("Redirected to delete user");
        try(Connection conn = ds.getConnection()){
            int userId = Integer.parseInt(ctx.pathParam("userId"));

            String sql = "DELETE FROM users WHERE id=?";
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setInt(1, userId);
                int rows = stmt.executeUpdate();

                if (rows > 0){
                    ctx.status(201).result("User deleted successfully");
                } else {
                    ctx.status(400).result("Failed to delete user");
                }
            }

        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }

    public static void editUser(Context ctx){
        logger.info("Redirected to update user");
        try(Connection conn = ds.getConnection()){
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            User user = ctx.bodyAsClass(User.class);
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());

            String sql = "UPDATE users SET username = ?, email = ?, password = ?, role = ? WHERE id = ?";
            try(PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, hashedPassword);
                stmt.setString(4, user.getRole());
                stmt.setInt(5, userId);

                int rows = stmt.executeUpdate();
                if (rows > 0){
                    ctx.status(201).result("User edited successfully");
                } else {
                    ctx.status(400).result("Failed to edit user");
                }
            }


        } catch (Exception e) {
            logger.error("Couldn't connect to databse: ", e);
            ctx.status(500).result("Database connection failed: " + e.getMessage());
        }
    }
}
