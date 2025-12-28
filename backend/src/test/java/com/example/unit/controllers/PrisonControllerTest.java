package com.example.unit.controllers;

import com.example.Controllers.PrisonController;
import com.example.Objects.Prison;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PrisonControllerTest {

    private DataSource mockDataSource;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private Context ctx;
    private PrisonController controller;

    @BeforeEach
    void setUp() throws SQLException {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        ctx = mock(Context.class);

        when(ctx.status(anyInt())).thenReturn(ctx);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        
        // Obsługa obu wariantów prepareStatement
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStatement);

        // Przygotowanie mocków dla wyników
        when(mockStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        controller = new PrisonController(mockDataSource);
    }

    private Prison createValidPrison() {
        Prison prison = new Prison();
        prison.setName("Test Prison");
        prison.setLocation("Test City");
        prison.setCapacity(500);
        prison.setSecurityLevel("Medium");
        prison.setNumOfCells(250);
        prison.setIsActive(true);
        return prison;
    }

    // ==================== addPrison Tests (Główna zmiana) ====================

    @Test
    void addPrison_shouldReturn201WhenPrisonAddedSuccessfully() throws SQLException {
        // given
        Prison prison = createValidPrison();
        when(ctx.bodyAsClass(Prison.class)).thenReturn(prison);
        when(mockStatement.executeUpdate()).thenReturn(1);
        
        // Symulacja zwracania wygenerowanego ID
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        // when
        controller.addPrison(ctx);

        // then
        verify(ctx).status(201);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        Map<String, Object> response = captor.getValue();
        assertEquals("Prison added successfully", response.get("message"));
        assertEquals(1, response.get("prison_id")); // Sprawdza czy zwraca ID
    }

    @Test
    void addPrison_shouldReturn400WhenNameIsEmpty() throws SQLException {
        // given
        Prison prison = createValidPrison();
        prison.setName("");
        when(ctx.bodyAsClass(Prison.class)).thenReturn(prison);

        // when
        controller.addPrison(ctx);

        // then
        verify(ctx).status(400);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        assertEquals("Prison name is required", captor.getValue().get("error"));
    }

    @Test
    void addPrison_shouldReturn500OnDatabaseError() throws SQLException {
        // given
        Prison prison = createValidPrison();
        when(ctx.bodyAsClass(Prison.class)).thenReturn(prison);
        // Symulujemy błąd przy próbie połączenia
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Conn failed"));

        // when
        controller.addPrison(ctx);

        // then
        verify(ctx).status(500);
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        // Dopasowane do Twojego nowego catch (Database error)
        assertEquals("Database error", captor.getValue().get("error"));
    }

    // ==================== getAllPrisons Tests ====================

    @Test
    void getAllPrisons_shouldReturn200WithPrisonList() throws SQLException {
        when(mockResultSet.next()).thenReturn(true).thenReturn(false);
        when(mockResultSet.getInt("prison_id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Prison A");
        when(mockResultSet.getString("location")).thenReturn("City A");
        when(mockResultSet.getInt("capacity")).thenReturn(500);
        when(mockResultSet.getString("security_level")).thenReturn("High");
        when(mockResultSet.getDate("opening_date")).thenReturn(Date.valueOf("2020-01-01"));
        when(mockResultSet.getInt("number_of_cells")).thenReturn(250);
        when(mockResultSet.getBoolean("is_active")).thenReturn(true);

        controller.getAllPrisons(ctx);

        verify(ctx).status(200);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(ctx).json(captor.capture());
        assertEquals(1, captor.getValue().size());
    }

    @Test
    void getAllPrisons_shouldReturn404WhenNoPrisonsFound() throws SQLException {
        when(mockResultSet.next()).thenReturn(false);
        controller.getAllPrisons(ctx);
        verify(ctx).status(404);
    }

    // ==================== deletePrison Tests ====================

    @Test
    void deletePrison_shouldReturn200WhenPrisonDeletedSuccessfully() throws SQLException {
        when(ctx.pathParam("prisonId")).thenReturn("1");
        when(mockStatement.executeUpdate()).thenReturn(1);

        controller.deletePrison(ctx);

        verify(ctx).status(200);
        verify(mockStatement).setInt(1, 1);
    }

    @Test
    void deletePrison_shouldReturn400ForInvalidPrisonId() throws SQLException {
        when(ctx.pathParam("prisonId")).thenReturn("abc");
        controller.deletePrison(ctx);
        verify(ctx).status(400);
    }

    // ==================== editPrison Tests ====================

    @Test
    void editPrison_shouldReturn200WhenPrisonEditedSuccessfully() throws SQLException {
        Prison prison = createValidPrison();
        when(ctx.pathParam("prisonId")).thenReturn("1");
        when(ctx.bodyAsClass(Prison.class)).thenReturn(prison);
        when(mockStatement.executeUpdate()).thenReturn(1);

        controller.editPrison(ctx);

        verify(ctx).status(200);
        // Sprawdza parametry: 6 pól + 7-my parametr to ID w WHERE
        verify(mockStatement).setInt(7, 1);
    }

    @Test
    void editPrison_shouldReturn404WhenNotFound() throws SQLException {
        Prison prison = createValidPrison();
        when(ctx.pathParam("prisonId")).thenReturn("99");
        when(ctx.bodyAsClass(Prison.class)).thenReturn(prison);
        when(mockStatement.executeUpdate()).thenReturn(0);

        controller.editPrison(ctx);

        verify(ctx).status(404);
    }
}