package com.example.Objects;

public class LoginRequest {
    private String username;
    private String password;
    
    // Konstruktor bez parametrów (WYMAGANY dla Javalina)
    public LoginRequest() {}
    
    // Konstruktor z parametrami (opcjonalny)
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Gettery (WYMAGANE)
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
    
    // Settery (WYMAGANE)
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
