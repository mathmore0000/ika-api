package ika.controllers.aux_classes.auth;

import jakarta.validation.constraints.NotNull;

public class AuthRequest {
    @NotNull(message = "Username is required")
    private String username;
    @NotNull(message = "Password is required")
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
