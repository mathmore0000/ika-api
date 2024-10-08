package ika.controllers.aux_classes.auth;

import jakarta.validation.constraints.NotNull;

public class RefreshTokenRequest {
    @NotNull(message = "Refresh Token is required")
    private String refreshToken;

    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
