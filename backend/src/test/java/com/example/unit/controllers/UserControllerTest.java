package com.example.unit.controllers;

import com.example.Controllers.UserController;
import com.example.Objects.LoginRequest;
import com.example.Objects.LoginResponse;
import com.example.Objects.User;
import io.javalin.http.Context;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.security.Key;
import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private DataSource mockDataSource;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private Context ctx;
    private UserController controller;
    private Key testSecretKey;

    @BeforeEach
    void setUp() throws SQLException {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        ctx = mock(Context.class);
        testSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        when(ctx.status(anyInt())).thenReturn(ctx);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        
        // Obsługa obu wersji prepareStatement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStatement);
        
        when(mockStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        controller = new UserController(mockDataSource, testSecretKey);
    }

    // ==================== addUser Tests (Najważniejsze poprawki) ====================

    @Test
    void addUser_shouldReturn201WhenUserAddedSuccessfully() throws SQLException {
        // given
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("newuser@test.com");
        user.setPassword("password123");
        user.setRole("user");
        
        when(ctx.bodyAsClass(User.class)).thenReturn(user);
        when(mockStatement.executeUpdate()).thenReturn(1);
        
        // Symulacja zwracania ID przez bazę
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(10);

        // when
        controller.addUser(ctx);

        // then
        verify(ctx).status(201);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        Map<String, Object> response = captor.getValue();
        assertEquals("User added successfully", response.get("message"));
        assertEquals(10, response.get("id")); // Test sprawdza nowe ID
    }

    @Test
    void addUser_shouldReturn400WhenFieldsAreEmpty() throws SQLException {
        // given - Twój kontroler teraz używa "All fields are required"
        User user = new User();
        user.setUsername(""); // Puste pole
        user.setEmail("test@test.com");
        user.setPassword("password123");
        
        when(ctx.bodyAsClass(User.class)).thenReturn(user);

        // when
        controller.addUser(ctx);

        // then
        verify(ctx).status(400);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        // Zmienione na nową treść błędu z kontrolera
        assertEquals("All fields are required", captor.getValue().get("error"));
    }

    @Test
    void addUser_shouldSetDefaultRoleWhenRoleIsNull() throws SQLException {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password123");
        user.setRole(null);
        
        when(ctx.bodyAsClass(User.class)).thenReturn(user);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);

        // when
        controller.addUser(ctx);

        // then
        verify(mockStatement).setString(eq(4), eq("user")); 
        verify(ctx).status(201);
    }

    @Test
    void addUser_shouldReturn500OnDatabaseError() throws SQLException {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@test.com");
        user.setPassword("password123");
        
        when(ctx.bodyAsClass(User.class)).thenReturn(user);
        when(mockDataSource.getConnection()).thenThrow(new SQLException("DB Fail"));

        // when
        controller.addUser(ctx);

        // then
        verify(ctx).status(500);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
    }

    // ==================== login Tests ====================

    @Test
    void login_shouldReturn200WithTokenForValidCredentials() throws SQLException {
        String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        
        when(ctx.bodyAsClass(LoginRequest.class)).thenReturn(loginRequest);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn(hashedPassword);
        when(mockResultSet.getString("username")).thenReturn("testuser");

        controller.login(ctx);

        verify(ctx).status(200);
        verify(ctx).json(any(LoginResponse.class));
    }

    @Test
    void login_shouldReturn401ForInvalidCredentials() throws SQLException {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrong");
        when(ctx.bodyAsClass(LoginRequest.class)).thenReturn(loginRequest);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("password")).thenReturn(BCrypt.hashpw("correct", BCrypt.gensalt()));

        controller.login(ctx);

        verify(ctx).status(401);
    }

    // ==================== getAllUsers Tests ====================

    @Test
    void getAllUsers_shouldReturn200WithUserList() throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("username")).thenReturn("user1");

        controller.getAllUsers(ctx);

        verify(ctx).status(200);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(ctx).json(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    // ==================== deleteUser Tests ====================

    @Test
    void deleteUser_shouldReturn200WhenUserDeletedSuccessfully() throws SQLException {
        when(ctx.pathParam("userId")).thenReturn("1");
        when(mockStatement.executeUpdate()).thenReturn(1);

        controller.deleteUser(ctx);

        verify(ctx).status(200);
    }

    @Test
    void deleteUser_shouldReturn400ForInvalidUserId() throws SQLException {
        when(ctx.pathParam("userId")).thenReturn("invalid");
        controller.deleteUser(ctx);
        verify(ctx).status(400);
    }
}