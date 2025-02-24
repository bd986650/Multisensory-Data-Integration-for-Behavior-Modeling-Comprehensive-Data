package multisensory.project;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;

@Component
public class JwtTokenUtil {

    // Загружаем секретную фразу из настроек (application.properties)
    @Value("${jwt.secret}")
    private String secretKeyString;  // секретная строка, которая задается в конфигурации

    private SecretKey secretKey; // ключ для подписи JWT
    private final long EXPIRATION_TIME = 86400000; // Время жизни токена (24 часа)

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secretKeyString);  // Декодируем строку из Base64
        secretKey = Keys.hmacShaKeyFor(keyBytes);  // Генерируем ключ из строки
    }

    // Генерация токена с UUID userId
    public String generateToken(UUID userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());  // Преобразуем UUID в строку для добавления в payload

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)  // Используем постоянный секретный ключ
                .compact();
    }

    // Получение userId из токена
    public UUID extractUserId(String token) {
        String tokenWithoutBearer = token.substring(7); // Убираем "Bearer " из токена
        Claims claims = getClaimsFromToken(tokenWithoutBearer);
        String userIdStr = claims.get("userId", String.class);  // Извлекаем userId как строку
        return UUID.fromString(userIdStr);  // Преобразуем строку в UUID
    }

    // Проверка срока действия токена
    public boolean isTokenExpired(String token) {
        String tokenWithoutBearer = token.substring(7); // Убираем "Bearer " из токена
        Date expiration = getClaimsFromToken(tokenWithoutBearer).getExpiration();
        return expiration.before(new Date());
    }

    // Получение всех claims (данных) из токена
    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
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
