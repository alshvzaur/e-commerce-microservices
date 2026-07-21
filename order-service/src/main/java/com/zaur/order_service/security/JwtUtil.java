package com.zaur.order_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtUtil {

    SecretKey secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    //генерация токена (берём username + когда выдан + дата истечения + подпписываем с secret_key) и собираем в JWT-строку
    public String generateToken(String username, String role, UUID userId) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("userId",  userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 *60))
                .signWith(secretKey)
                .compact();
    }

    // извлечение имени пользователя из токена
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role").toString();
    }

    // извлечение ид из токена
    public String extractUserId(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    // проверка истечение токена. true если дата уже в прошлом
    boolean isExpired(String token){
        return parseClaims(token).getExpiration().before(new Date());
    }

    // токен валиден если: username совпадает И токен ещё не истёк
    public boolean isValid(String token, String username) {
        return extractUsername(token).equals(username) && !isExpired(token);
    }

    Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey) // говорим каким ключом проверять подпись
                .build()
                .parseSignedClaims(token)  // парсим токен + проверяем подпись
                .getPayload(); // достаём содержимое (claims)
    }

}
