package com.example.demo;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenUtil {

    // Генерация безопасного ключа, гарантированно длиной >= 512 бит
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    private final long EXPIRATION_TIME = 86400000;

    // Генерация токена с UUID userId
    public String generateToken(UUID userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());  // Преобразуем UUID в строку для добавления в payload

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)  // Используйте секретный ключ при генерации
                .compact();
    }

    // Получение userId из токена
    public UUID extractUserId(String token) {
        Claims claims = getClaimsFromToken(token);
        System.out.println("2: " + claims);
        String userIdStr = claims.get("userId", String.class);  // Извлекаем userId как строку
        return UUID.fromString(userIdStr);  // Преобразуем строку в UUID
    }

    // Получение имени пользователя из токена
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getSubject();
    }

    // Проверка токена
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    // Проверка срока действия токена
    private boolean isTokenExpired(String token) {
        Date expiration = getClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }

    // Получение всех claims (данных) из токена
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("JWT token is expired", e);  // Обработка ошибки истечения токена
        } catch (SignatureException e) {
            throw new RuntimeException("JWT signature does not match", e);  // Обработка ошибки подписи
        } catch (JwtException e) {
            throw new RuntimeException("JWT token is invalid", e);  // Обработка других ошибок токена
        }
    }


}
