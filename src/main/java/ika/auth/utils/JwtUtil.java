package ika.auth.utils;

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

@Service
public class JwtUtil {

    // Carregar as chaves do .env
    @Value("${JWT_SECRET_GENERATE}")
    private String secretKeyGenerate; // Chave usada para gerar o token

    @Value("${JWT_SECRET_VALIDATE}")
    private String secretKeyValidate; // Chave usada para validar o token

    // Método para converter string em chave HMAC
    private Key getSigningKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    // Gera o token JWT usando as informações do UserDetails
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>(); // Você pode adicionar claims extras aqui
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
                .signWith(getSigningKey(secretKeyGenerate), SignatureAlgorithm.HS256) // Assina o token com a chave de geração
                .compact();
    }

    // Extrai as claims do token
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey(secretKeyValidate)) // Usa a chave de validação
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

    // Verifica se o token expirou
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
