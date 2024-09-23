package ika.auth.controllers;

public class AuthRequest {
    private String username;
    private String password;

    // Construtor padrão
    public AuthRequest() {}

    // Getters e Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
