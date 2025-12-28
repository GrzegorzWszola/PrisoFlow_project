package com.example.Controllers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
 * The {@code UserController} class handles user authentication and CRUD operations.
 *
 * <p>This controller provides endpoints for user login, registration, and management.
 * It includes JWT token generation and BCrypt password hashing.
 *
 * @see com.example.Objects.User
 */
public class UserController {
    private final DataSource dataSource;
    private final Logger logger;
    private final Key secretKey;

    /**
     * Constructor with dependency injection for testing.
     *
     * @param dataSource the data source to use for database connections
     * @param secretKey the secret key for JWT token generation
     */
    public UserController(DataSource dataSource, Key secretKey) {
        this.dataSource = dataSource;
        this.logger = LoggerFactory.getLogger(UserController.class);
        this.secretKey = secretKey;
    }

    /**
     * Constructor with DataSource only (generates default secret key).
     *
     * @param dataSource the data source to use for database connections
     */
    public UserController(DataSource dataSource) {
        this(dataSource, Keys.secretKeyFor(SignatureAlgorithm.HS256));
    }

    /**
     * Default constructor for production use.
     */
    public UserController() {
        this(DatabaseConfig.getDataSource());
    }

    /**
     * Authenticates a user by username and password.
     *
     * @param conn the database connection
     * @param username the username
     * @param password the plain text password
     * @return the authenticated User object, or null if authentication fails
     * @throws SQLException if a database error occurs
     */
    private User authenticateUser(Connection conn, String username, String password) 
            throws SQLException {
        String sql = "SELECT id, username, email, password, role FROM users WHERE username = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                
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

    /**
     * Generates a JWT token for the given user.
     *
     * @param user the user for whom to generate the token
     * @return the JWT token string
     */
    private String generateToken(User user) {
        long expirationTime = 86400000; // 24 hours in milliseconds
        
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Handles a simple health-check or test request.
     *
     * @param ctx the {@link Context} object representing the HTTP request and response
     */
    public void helloMsg(Context ctx) {
        try (Connection conn = dataSource.getConnection()) {
            ctx.status(200).result("Hello from backend");
        } catch (Exception e) {
            logger.error("Couldn't connect to database: ", e);
            ctx.status(500).json(Map.of(
                "error", "Database connection failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Handles user login authentication.
     *
     * @param ctx the {@link Context} object containing login credentials
     */
    public void login(Context ctx) {
        try (Connection conn = dataSource.getConnection()) {
            LoginRequest loginData = ctx.bodyAsClass(LoginRequest.class);
            
            String username = loginData.getUsername();
            String password = loginData.getPassword();
            
            // Validate input
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                ctx.status(400).json(Map.of("error", "Username and password are required"));
                return;
            }
            
            logger.info("Login attempt for user: {}", username);
            
            User user = authenticateUser(conn, username, password);
            
            if (user != null) {
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
            logger.error("Login error: ", e);
            ctx.status(500).json(Map.of(
                "error", "Database connection failed",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Retrieves all users from the database.
     *
     * @param ctx the {@link Context} object
     */
    public void getAllUsers(Context ctx) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM users";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                List<User> usersList = new ArrayList<>();

                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    // Don't include password in response
                    usersList.add(user);
                }

                ctx.status(200).json(usersList);    
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
     * Adds a new user to the database.
     *
     * @param ctx the {@link Context} object containing user data
     */
    public void addUser(Context ctx) {
        try (Connection conn = dataSource.getConnection()) {
            User user = ctx.bodyAsClass(User.class);
            
            // Walidacja... (zostaje bez zmian)
            if (user.getUsername() == null || user.getUsername().isEmpty() || 
                user.getPassword() == null || user.getPassword().isEmpty() || 
                user.getEmail() == null || user.getEmail().isEmpty()) {
                ctx.status(400).json(Map.of("error", "All fields are required"));
                return;
            }
            
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            String sql = "INSERT INTO users(username, email, password, role) VALUES (?, ?, ?, ?)";

            // DODANO: Statement.RETURN_GENERATED_KEYS
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, hashedPassword);
                stmt.setString(4, user.getRole() != null ? user.getRole() : "user");
                
                stmt.executeUpdate();

                // POBIERANIE GENEROWANEGO ID
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        ctx.status(201).json(Map.of(
                            "message", "User added successfully",
                            "id", newId // Tego szuka test
                        ));
                    } else {
                        ctx.status(400).json(Map.of("message", "Failed to add user, no ID generated"));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error adding user: ", e);
            ctx.status(500).json(Map.of("error", "Database error", "message", e.getMessage()));
        }
    }

    /**
     * Deletes a user by ID.
     *
     * @param ctx the {@link Context} object containing the user ID
     */
    public void deleteUser(Context ctx) {
        logger.info("Redirected to delete user");
        try (Connection conn = dataSource.getConnection()) {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            String sql = "DELETE FROM users WHERE id=?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    ctx.status(200).json(Map.of("message", "User deleted successfully"));
                } else {
                    ctx.status(404).json(Map.of("message", "User not found"));
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format: ", e);
            ctx.status(400).json(Map.of(
                "error", "Invalid user ID",
                "message", "User ID must be a number"
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
     * Updates an existing user.
     *
     * @param ctx the {@link Context} object containing the user ID and updated data
     */
    public void editUser(Context ctx) {
        logger.info("Redirected to update user");
        try (Connection conn = dataSource.getConnection()) {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            User user = ctx.bodyAsClass(User.class);
            
            // Validate input
            if (user.getUsername() == null || user.getUsername().isEmpty()) {
                ctx.status(400).json(Map.of("error", "Username is required"));
                return;
            }
            
            String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
            String sql = "UPDATE users SET username = ?, email = ?, password = ?, role = ? WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, hashedPassword);
                stmt.setString(4, user.getRole());
                stmt.setInt(5, userId);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    ctx.status(200).json(Map.of("message", "User edited successfully"));
                } else {
                    ctx.status(404).json(Map.of("message", "User not found"));
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format: ", e);
            ctx.status(400).json(Map.of(
                "error", "Invalid user ID",
                "message", "User ID must be a number"
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