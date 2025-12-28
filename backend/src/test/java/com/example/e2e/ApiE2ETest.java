package com.example.e2e;

import com.example.App;
import com.example.Config.DatabaseConfig;
import com.example.Objects.LoginRequest;
import com.example.Objects.Prison;
import com.example.Objects.User;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * End-to-End tests for the entire API.
 * These tests require a running database and test the full application stack.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiE2ETest {

    private static Javalin app;
    private static final int TEST_PORT = 8080;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private static String authToken;
    private static int createdUserId;
    private static int createdPrisonId;

    private static void resetSequences() {
        try (Connection conn = DatabaseConfig.getConnection(); // użyj swojej metody do połączenia
             Statement stmt = conn.createStatement()) {
            
            // Reset dla tabeli users
            stmt.execute("SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE(MAX(id), 0) + 1, false) FROM users");
            
            // Reset dla tabeli prisons
            stmt.execute("SELECT setval(pg_get_serial_sequence('prisons', 'prison_id'), COALESCE(MAX(prison_id), 0) + 1, false) FROM prisons");
            
        } catch (SQLException e) {
            System.err.println("Błąd podczas resetowania sekwencji: " + e.getMessage());
        }
    }

    @BeforeAll
    static void setUp() {
        // Uruchom aplikację
        app = App.createApp();
        app.start(TEST_PORT);
        
        // Poczekaj aż serwer się uruchomi
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    static void tearDown() {
        if (app != null) {
            app.stop();
            resetSequences();
        }
    }


    // ==================== Health Check Tests ====================

    @Test
    @Order(1)
    void testHealthCheck() {
        HttpResponse<String> response = Unirest.get(BASE_URL + "/api/hello")
            .asString();

        assertEquals(200, response.getStatus());
        assertEquals("Hello from backend", response.getBody());
    }

    @Test
    @Order(2)
    void testDatabaseHealth() {
        HttpResponse<String> response = Unirest.get(BASE_URL + "/api/db/health")
            .asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains("healthy"));
    }

    // ==================== Authentication Tests ====================

    @Test
    @Order(3)
    void testLoginWithValidCredentials() {
        HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "/api/login")
            .header("Content-Type", "application/json")
            .body(new JSONObject()
                .put("username", "admin")
                .put("password", "admin123"))
            .asJson();

        assertEquals(200, response.getStatus());
        
        JSONObject body = response.getBody().getObject();
        assertTrue(body.getBoolean("success"));
        assertEquals("admin", body.getString("username"));
        assertNotNull(body.getString("token"));
        
        // Zapisz token do użycia w innych testach
        authToken = body.getString("token");
    }

    @Test
    @Order(4)
    void testLoginWithInvalidCredentials() {
        HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "/api/login")
            .header("Content-Type", "application/json")
            .body(new JSONObject()
                .put("username", "admin")
                .put("password", "wrongpassword"))
            .asJson();

        assertEquals(401, response.getStatus());
        
        JSONObject body = response.getBody().getObject();
        assertEquals("Invalid credentials", body.getString("error"));
    }

    @Test
    @Order(5)
    void testLoginWithEmptyCredentials() {
        HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "/api/login")
            .header("Content-Type", "application/json")
            .body(new JSONObject()
                .put("username", "")
                .put("password", ""))
            .asJson();

        assertEquals(400, response.getStatus());
    }

    // ==================== User CRUD Tests ====================

    @Test
    @Order(10)
    void testGetAllUsers() {
        HttpResponse<JsonNode> response = Unirest.get(BASE_URL + "/api/users")
            .header("Authorization", "Bearer " + authToken)
            .asJson();

        assertEquals(200, response.getStatus());
        
        JSONArray users = response.getBody().getArray();
        assertTrue(users.length() > 0);
    }

    @Test
    @Order(11)
    void testAddUser() {
        // 1. Dodawanie użytkownika
        HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "/api/users")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + authToken)
            .body(new JSONObject()
                .put("username", "testuser_e2e")
                .put("email", "testuser@e2e.com")
                .put("password", "password123")
                .put("role", "user"))
            .asJson();

        assertEquals(201, response.getStatus());
        
        JSONObject body = response.getBody().getObject();
        // Pobieramy ID, żeby wiedzieć co usunąć
        int userIdToDelete = body.getInt("id"); 

        // 2. Sprzątanie (Usuwanie)
        HttpResponse<JsonNode> deleteResponse = Unirest.delete(BASE_URL + "/api/users/" + userIdToDelete)
            .header("Authorization", "Bearer " + authToken)
            .asJson();

        assertEquals(200, deleteResponse.getStatus(), "Cleanup failed: User was not deleted");
    }

    @Test
    @Order(12)
    void testAddUserWithMissingFields() {
        HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "/api/users")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + authToken)
            .body(new JSONObject()
                .put("username", "incompleteuser"))
            .asJson();

        assertEquals(400, response.getStatus());
    }

    // ==================== Prison CRUD Tests ====================

    @Test
    @Order(20)
    void testGetAllPrisons() {
        HttpResponse<JsonNode> response = Unirest.get(BASE_URL + "/api/prisons")
            .header("Authorization", "Bearer " + authToken)
            .asJson();

        // Może być 200 z listą lub 404 jeśli brak więzień
        assertTrue(response.getStatus() == 200 || response.getStatus() == 404);
    }

    @Test
    @Order(21)
    void testAddPrison() {
        // 1. Dodawanie więzienia
        HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "/api/prisons")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + authToken)
            .body(new JSONObject()
                .put("name", "E2E Test Prison")
                .put("location", "Test City")
                .put("capacity", 500)
                .put("securityLevel", "low") // Upewnij się, że "Low" jest dozwolone w bazie (CHECK constraint)
                .put("numOfCells", 250)
                .put("isActive", true))
            .asJson();

        assertEquals(201, response.getStatus(), "Failed to add prison: " + response.getBody());
        
        JSONObject body = response.getBody().getObject();
        assertEquals("Prison added successfully", body.getString("message"));

        // Pobieramy ID nowo utworzonego więzienia
        // Upewnij się, że Twoje API zwraca klucz "prisonId" lub "id"
        int createdPrisonId = body.getInt("prison_id"); 

        // 2. Sprzątanie - Usuwamy to więzienie zaraz po teście
        HttpResponse<JsonNode> deleteResponse = Unirest.delete(BASE_URL + "/api/prisons/" + createdPrisonId)
            .header("Authorization", "Bearer " + authToken)
            .asJson();

        assertEquals(200, deleteResponse.getStatus(), "Cleanup failed: Prison was not deleted");
    }

    @Test
    @Order(22)
    void testAddPrisonWithMissingName() {
        HttpResponse<JsonNode> response = Unirest.post(BASE_URL + "/api/prisons")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + authToken)
            .body(new JSONObject()
                .put("location", "Test City")
                .put("capacity", 500))
            .asJson();

        assertEquals(400, response.getStatus());
        
        JSONObject body = response.getBody().getObject();
        assertEquals("Prison name is required", body.getString("error"));
    }

    // ==================== Dashboard Tests ====================

    @Test
    @Order(30)
    void testDashboard() {
        HttpResponse<JsonNode> response = Unirest.get(BASE_URL + "/api/db/dashboard")
            .header("Authorization", "Bearer " + authToken)
            .asJson();

        assertEquals(200, response.getStatus());
        
        JSONObject body = response.getBody().getObject();
        assertTrue(body.has("prisons"));
        assertTrue(body.has("visits"));
        assertTrue(body.has("incidents"));
    }

    // ==================== Admin Tests ====================

    @Test
    @Order(40)
    void testAdminEndpoint() {
        HttpResponse<String> response = Unirest.get(BASE_URL + "/api/admin/test")
            .header("Authorization", "Bearer " + authToken)
            .asString();

        assertEquals(200, response.getStatus());
        assertTrue(response.getBody().contains("admin"));
    }

    // ==================== Error Handling Tests ====================

    @Test
    @Order(50)
    void testInvalidEndpoint() {
        HttpResponse<String> response = Unirest.get(BASE_URL + "/api/nonexistent")
            .asString();

        assertEquals(404, response.getStatus());
    }

    @Test
    @Order(51)
    void testDeleteNonexistentPrison() {
        HttpResponse<JsonNode> response = Unirest.delete(BASE_URL + "/api/prisons/99999")
            .header("Authorization", "Bearer " + authToken)
            .asJson();

        assertEquals(404, response.getStatus());
    }

    @Test
    @Order(52)
    void testInvalidPrisonId() {
        HttpResponse<JsonNode> response = Unirest.delete(BASE_URL + "/api/prisons/invalid")
            .header("Authorization", "Bearer " + authToken)
            .asJson();

        assertEquals(400, response.getStatus());
        
        JSONObject body = response.getBody().getObject();
        assertEquals("Invalid prison ID", body.getString("error"));
    }

    // ==================== Integration Flow Tests ====================

    @Test
    @Order(60)
    void testCompleteUserFlow() {
        // 1. Create user
        HttpResponse<JsonNode> createResponse = Unirest.post(BASE_URL + "/api/users")
            .header("Content-Type", "application/json")
            .body(new JSONObject()
                .put("username", "flowtest")
                .put("email", "flow@test.com")
                .put("password", "pass123")
                .put("role", "user"))
            .asJson();

        assertEquals(201, createResponse.getStatus());
        
        // WYCIĄGAMY ID stworzonego użytkownika (zakładając, że API je zwraca)
        // Jeśli Twoje API nie zwraca ID w body, musisz je pobrać z bazy lub listy
        int createdUserId = createResponse.getBody().getObject().getInt("id");

        // 2. Login as new user
        HttpResponse<JsonNode> loginResponse = Unirest.post(BASE_URL + "/api/login")
            .header("Content-Type", "application/json")
            .body(new JSONObject()
                .put("username", "flowtest")
                .put("password", "pass123"))
            .asJson();

        assertEquals(200, loginResponse.getStatus());
        String userToken = loginResponse.getBody().getObject().getString("token");

        // 3. Access protected resource
        HttpResponse<JsonNode> protectedResponse = Unirest.get(BASE_URL + "/api/prisons")
            .header("Authorization", "Bearer " + userToken)
            .asJson();
        assertTrue(protectedResponse.getStatus() == 200 || protectedResponse.getStatus() == 404);

        // 4. SPRZĄTANIE: Usuwamy użytkownika
        // Używamy authToken admina (jeśli user nie może usunąć sam siebie)
        HttpResponse<JsonNode> deleteResponse = Unirest.delete(BASE_URL + "/api/users/" + createdUserId)
            .header("Authorization", "Bearer " + authToken) // używamy tokenu admina z Order(3)
            .asJson();

        assertEquals(200, deleteResponse.getStatus(), "Failed to clean up user after test");
    }

    @Test
    @Order(61)
    void testCompletePrisonFlow() {
        // 1. Add prison
        HttpResponse<JsonNode> addResponse = Unirest.post(BASE_URL + "/api/prisons")
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + authToken)
            .body(new JSONObject()
                .put("name", "Flow Test Prison")
                .put("location", "Flow City")
                .put("capacity", 300)
                .put("securityLevel", "medium")
                .put("numOfCells", 150)
                .put("isActive", true))
            .asJson();

        assertEquals(201, addResponse.getStatus());

        // 2. Get all prisons (should include new one)
        HttpResponse<JsonNode> getAllResponse = Unirest.get(BASE_URL + "/api/prisons")
            .header("Authorization", "Bearer " + authToken)
            .asJson();

        assertEquals(200, getAllResponse.getStatus());
        JSONArray prisons = getAllResponse.getBody().getArray();
        
        boolean found = false;
        int prisonId = -1;
        for (int i = 0; i < prisons.length(); i++) {
            JSONObject prison = prisons.getJSONObject(i);
            if ("Flow Test Prison".equals(prison.getString("name"))) {
                found = true;
                prisonId = prison.getInt("id");
                break;
            }
        }
        assertTrue(found, "Newly created prison should be in the list");

        // 3. Update prison (if we found its ID)
        if (prisonId > 0) {
            HttpResponse<JsonNode> updateResponse = Unirest.put(BASE_URL + "/api/prisons/" + prisonId)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .body(new JSONObject()
                    .put("name", "Flow Test Prison Updated")
                    .put("location", "Flow City Updated")
                    .put("capacity", 400)
                    .put("securityLevel", "high")
                    .put("numOfCells", 200)
                    .put("isActive", false))
                .asJson();

            assertEquals(200, updateResponse.getStatus());

            // 4. Delete prison
            HttpResponse<JsonNode> deleteResponse = Unirest.delete(BASE_URL + "/api/prisons/" + prisonId)
                .header("Authorization", "Bearer " + authToken)
                .asJson();

            assertEquals(200, deleteResponse.getStatus());
        }
    }
}