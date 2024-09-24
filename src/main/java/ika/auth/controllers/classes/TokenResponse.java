package ika.auth.controllers.classes;

public class TokenResponse {
    private String jwt;
    private String refreshToken;

    public TokenResponse(String jwt, String refreshToken) {
        this.jwt = jwt;
        this.refreshToken = refreshToken;
    }

    public String getJwt() {
        return jwt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
