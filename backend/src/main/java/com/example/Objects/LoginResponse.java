package com.example.Objects;

public class LoginResponse {
    private boolean success;
    private String message;
    private String username;
    private String email;
    private String role;
    private String token;
    
    // Konstruktor
    public LoginResponse(boolean success, String message, String username, 
                        String email, String role, String token) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.email = email;
        this.role = role;
        this.token = token;
    }
    
    // Gettery
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getRole() {
        return role;
    }
    
    public String getToken() {
        return token;
    }
    
    // Settery (opcjonalne)
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
}