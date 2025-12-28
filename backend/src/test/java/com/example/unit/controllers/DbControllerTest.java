// src/test/java/com/example/unit/controllers/DbControllerTest.java
package com.example.unit.controllers;

import com.example.Controllers.DbController;
import com.example.Objects.Prison;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DbControllerTest {

    private DataSource mockDataSource;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;
    private Context ctx;
    private DbController controller;

    @BeforeEach
    void setUp() throws SQLException {
        mockDataSource = mock(DataSource.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        ctx = mock(Context.class);

        when(ctx.status(anyInt())).thenReturn(ctx);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        controller = new DbController(mockDataSource);
    }

    // ==================== checkHealth Tests ====================

    @Test
    void checkHealth_shouldReturn200WhenDatabaseIsHealthy() throws SQLException {
        // when
        controller.checkHealth(ctx);

        // then
        verify(ctx).status(200);
        verify(ctx).result("Database connection is healthy!");
        verify(mockDataSource).getConnection();
    }

    @Test
    void checkHealth_shouldReturn500WhenDatabaseConnectionFails() throws SQLException {
        // given
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        // when
        controller.checkHealth(ctx);

        // then
        verify(ctx).status(500);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ctx).result(captor.capture());
    }

    // ==================== getPrisonsFromDb Tests ====================

    @Test
    void getPrisonsFromDb_shouldReturnListOfPrisons() throws SQLException {
        // given
        when(mockResultSet.next())
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);

        // First prison
        when(mockResultSet.getInt("prison_id"))
            .thenReturn(1)
            .thenReturn(2);
        when(mockResultSet.getString("name"))
            .thenReturn("Prison A")
            .thenReturn("Prison B");
        when(mockResultSet.getString("location"))
            .thenReturn("City A")
            .thenReturn("City B");
        when(mockResultSet.getInt("capacity"))
            .thenReturn(500)
            .thenReturn(300);
        when(mockResultSet.getString("security_level"))
            .thenReturn("High")
            .thenReturn("Medium");
        when(mockResultSet.getDate("opening_date"))
            .thenReturn(Date.valueOf("2020-01-01"))
            .thenReturn(null);
        when(mockResultSet.getInt("number_of_cells"))
            .thenReturn(250)
            .thenReturn(150);
        when(mockResultSet.getBoolean("is_active"))
            .thenReturn(true)
            .thenReturn(true);

        // when
        List<Prison> result = controller.getPrisonsFromDb(ctx);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first prison
        Prison prison1 = result.get(0);
        assertEquals(1, prison1.getId());
        assertEquals("Prison A", prison1.getName());
        assertEquals("City A", prison1.getLocation());
        assertEquals(500, prison1.getCapacity());
        assertEquals("High", prison1.getSecurityLevel());
        assertEquals(LocalDate.of(2020, 1, 1), prison1.getDate());
        assertEquals(250, prison1.getNumOfCells());
        assertTrue(prison1.getIsActive());

        // Verify second prison
        Prison prison2 = result.get(1);
        assertEquals(2, prison2.getId());
        assertEquals("Prison B", prison2.getName());
        assertNull(prison2.getDate()); // opening_date was null
    }

    @Test
    void getPrisonsFromDb_shouldReturnEmptyListWhenNoData() throws SQLException {
        // given
        when(mockResultSet.next()).thenReturn(false);

        // when
        List<Prison> result = controller.getPrisonsFromDb(ctx);

        // then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getPrisonsFromDb_shouldReturnNullOnDatabaseError() throws SQLException {
        // given
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Connection error"));

        // when
        List<Prison> result = controller.getPrisonsFromDb(ctx);

        // then
        assertNull(result);
        verify(ctx).status(500);
    }

    // ==================== prisonInfo Tests ====================

    @Test
    void prisonInfo_shouldReturn200WithPrisonList() throws SQLException {
        // given
        when(mockResultSet.next())
            .thenReturn(true)
            .thenReturn(false);
        
        when(mockResultSet.getInt("prison_id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Test Prison");
        when(mockResultSet.getString("location")).thenReturn("Test City");
        when(mockResultSet.getInt("capacity")).thenReturn(100);
        when(mockResultSet.getString("security_level")).thenReturn("High");
        when(mockResultSet.getDate("opening_date")).thenReturn(null);
        when(mockResultSet.getInt("number_of_cells")).thenReturn(50);
        when(mockResultSet.getBoolean("is_active")).thenReturn(true);

        // when
        controller.prisonInfo(ctx);

        // then
        verify(ctx).status(200);
        
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(ctx).json(captor.capture());
        
        List<Prison> prisons = captor.getValue();
        assertEquals(1, prisons.size());
        assertEquals("Test Prison", prisons.get(0).getName());
    }

    @Test
    void prisonInfo_shouldReturn404WhenPrisonListIsEmpty() throws SQLException {
        // given
        when(mockResultSet.next()).thenReturn(false);

        // when
        controller.prisonInfo(ctx);

        // then
        verify(ctx).status(404);
        
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        Map<String, String> response = captor.getValue();
        assertEquals("Error with prisons table", response.get("error"));
    }

    @Test
    void prisonInfo_shouldReturn404WhenPrisonListIsNull() throws SQLException {
        // given
        when(mockDataSource.getConnection()).thenThrow(new SQLException("Error"));

        // when
        controller.prisonInfo(ctx);

        // then
        verify(ctx).status(404);
        
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        assertEquals("Error with prisons table", captor.getValue().get("error"));
    }

    // ==================== dashboard Tests ====================

    @Test
    void dashboard_shouldReturn200WithCompleteData() throws SQLException {
        // given - mock dla view_prison_occupancy
        PreparedStatement stmt1 = mock(PreparedStatement.class);
        ResultSet rs1 = mock(ResultSet.class);
        
        // mock dla prison_visits
        PreparedStatement stmt2 = mock(PreparedStatement.class);
        ResultSet rs2 = mock(ResultSet.class);
        
        // mock dla view_recent_incidents
        PreparedStatement stmt3 = mock(PreparedStatement.class);
        ResultSet rs3 = mock(ResultSet.class);

        // Setup dla pierwszego query (prisons)
        when(mockConnection.prepareStatement(contains("view_prison_occupancy")))
            .thenReturn(stmt1);
        when(stmt1.executeQuery()).thenReturn(rs1);
        when(rs1.next())
            .thenReturn(true)
            .thenReturn(false);
        when(rs1.getInt("prison_id")).thenReturn(1);
        when(rs1.getString("prison_name")).thenReturn("Test Prison");
        when(rs1.getString("location")).thenReturn("Test City");
        when(rs1.getInt("capacity")).thenReturn(500);
        when(rs1.getString("security_level")).thenReturn("High");
        when(rs1.getInt("current_inmates")).thenReturn(400);
        when(rs1.getDouble("occupancy_percentage")).thenReturn(80.0);

        // Setup dla drugiego query (visits)
        when(mockConnection.prepareStatement(contains("prison_visits")))
            .thenReturn(stmt2);
        when(stmt2.executeQuery()).thenReturn(rs2);
        when(rs2.next())
            .thenReturn(true)
            .thenReturn(false);
        when(rs2.getString("first_name")).thenReturn("John");
        when(rs2.getString("last_name")).thenReturn("Doe");
        when(rs2.getString("prison_name")).thenReturn("Test Prison");
        when(rs2.getString("visitor_first_name")).thenReturn("Jane");
        when(rs2.getString("visitor_last_name")).thenReturn("Doe");
        when(rs2.getString("relationship")).thenReturn("Sister");
        when(rs2.getTimestamp("visit_datetime")).thenReturn(new Timestamp(System.currentTimeMillis()));

        // Setup dla trzeciego query (incidents)
        when(mockConnection.prepareStatement(contains("view_recent_incidents")))
            .thenReturn(stmt3);
        when(stmt3.executeQuery()).thenReturn(rs3);
        when(rs3.next())
            .thenReturn(true)
            .thenReturn(false);
        when(rs3.getInt("incident_id")).thenReturn(1);
        when(rs3.getString("prison_name")).thenReturn("Test Prison");
        when(rs3.getString("incident_type")).thenReturn("Fight");
        when(rs3.getString("severity")).thenReturn("High");
        when(rs3.getString("criminal_involved")).thenReturn("John Doe");
        when(rs3.getString("officer_involved")).thenReturn("Officer Smith");
        when(rs3.getTimestamp("incident_datetime")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(rs3.getString("description")).thenReturn("Test incident");

        // when
        controller.dashboard(ctx);

        // then
        verify(ctx).status(200);
        
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        Map<String, Object> response = captor.getValue();
        assertNotNull(response.get("prisons"));
        assertNotNull(response.get("visits"));
        assertNotNull(response.get("incidents"));

        List<Map<String, Object>> prisons = (List<Map<String, Object>>) response.get("prisons");
        assertEquals(1, prisons.size());
        assertEquals("Test Prison", prisons.get(0).get("name"));
        assertEquals(80.0, prisons.get(0).get("occupancyPercentage"));

        List<Map<String, Object>> visits = (List<Map<String, Object>>) response.get("visits");
        assertEquals(1, visits.size());
        assertEquals("John", visits.get(0).get("criminal_first_name"));

        List<Map<String, Object>> incidents = (List<Map<String, Object>>) response.get("incidents");
        assertEquals(1, incidents.size());
        assertEquals("Fight", incidents.get(0).get("incident_type"));
    }

    @Test
    void dashboard_shouldReturn500OnDatabaseError() throws SQLException {
        // given
        when(mockConnection.prepareStatement(anyString()))
            .thenThrow(new SQLException("Database error"));

        // when
        controller.dashboard(ctx);

        // then
        verify(ctx).status(500);
        
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        Map<String, String> response = captor.getValue();
        assertEquals("Database error", response.get("error"));
        assertNotNull(response.get("message"));
    }

    @Test
    void dashboard_shouldHandleEmptyResultSets() throws SQLException {
        // given - wszystkie ResultSety puste
        PreparedStatement stmt1 = mock(PreparedStatement.class);
        PreparedStatement stmt2 = mock(PreparedStatement.class);
        PreparedStatement stmt3 = mock(PreparedStatement.class);
        
        ResultSet rs1 = mock(ResultSet.class);
        ResultSet rs2 = mock(ResultSet.class);
        ResultSet rs3 = mock(ResultSet.class);

        when(mockConnection.prepareStatement(contains("view_prison_occupancy"))).thenReturn(stmt1);
        when(mockConnection.prepareStatement(contains("prison_visits"))).thenReturn(stmt2);
        when(mockConnection.prepareStatement(contains("view_recent_incidents"))).thenReturn(stmt3);

        when(stmt1.executeQuery()).thenReturn(rs1);
        when(stmt2.executeQuery()).thenReturn(rs2);
        when(stmt3.executeQuery()).thenReturn(rs3);

        when(rs1.next()).thenReturn(false);
        when(rs2.next()).thenReturn(false);
        when(rs3.next()).thenReturn(false);

        // when
        controller.dashboard(ctx);

        // then
        verify(ctx).status(200);
        
        ArgumentCaptor<Map> captor = ArgumentCaptor.forClass(Map.class);
        verify(ctx).json(captor.capture());
        
        Map<String, Object> response = captor.getValue();
        
        List<Map<String, Object>> prisons = (List<Map<String, Object>>) response.get("prisons");
        List<Map<String, Object>> visits = (List<Map<String, Object>>) response.get("visits");
        List<Map<String, Object>> incidents = (List<Map<String, Object>>) response.get("incidents");

        assertEquals(0, prisons.size());
        assertEquals(0, visits.size());
        assertEquals(0, incidents.size());
    }

    // ==================== Constructor Tests ====================

    @Test
    void constructor_shouldCreateInstanceWithDataSource() {
        // given & when
        DbController controller = new DbController(mockDataSource);

        // then
        assertNotNull(controller);
    }
}