package ika.utils;

import ika.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtUtil {

    // Carregar as chaves do .env
    @Value("${JWT_SECRET_KEY}")
    private String secretKey; // Chave usada para validar o token

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // Expira em 7 dias
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256) // Usa a chave de geração
                .compact();
    }

    // Método para validar o refresh token
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey(secretKey)) // Usa a chave de validação
                    .build()
                    .parseClaimsJws(refreshToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Método para converter string em chave HMAC
    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    // Gera o token JWT usando as informações do UserDetails
    public String generateToken(UserDetails userDetails, User user) {
        Map<String, Object> claims = new HashMap<>(); // Você pode adicionar claims extras aqui
        claims.put("userId", user.getId()); // Adiciona as roles do usuário
        claims.put("locale", user.getLocale()); // Adiciona as roles do usuário
        claims.put("displayName", user.getDisplayName());
        claims.put("phoneNumber", user.getPhoneNumber());
        claims.put("dateOfBirth", user.getBirthDate());
        claims.put("avatarUrl", user.getAvatarUrl());
        claims.put("roles", userDetails.getAuthorities()); // Adiciona as roles do usuário
        return createToken(claims, userDetails.getUsername());
    }

    // Cria o token JWT com as claims e o username
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Token válido por 10 horas
                .signWith(getSigningKey(secretKey), SignatureAlgorithm.HS256) // Assina o token com a chave de geração
                .compact();
    }

    // Extrai as claims do token
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secretKey)) // Usa a chave de validação
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extrai o username do token JWT
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Valida o token com base no username
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public UUID extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return UUID.fromString(claims.get("userId", String.class)); // Extract the userId from the claims
    }

    // Verifica se o token expirou
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
