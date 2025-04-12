package multisensory.project.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class JwtTokenService {

    // Загружаем секретную фразу из настроек (application.properties)
    @Value("${jwt.secret}")
    private String accessSecretKeyString;

    @Value("${refresh.jwt.secret}")
    private String refreshSecretKeyString;

    private SecretKey secretKeyAccess; // ключ для подписи access токена
    private SecretKey secretKeyRefresh; // ключ для подписи refresh токена
    private final long EXPIRATION_TIME_ACCESS = 900000; // Время жизни токена (15 минут)


    @PostConstruct
    public void init() {
        byte[] keyBytesAccess = Base64.getDecoder().decode(accessSecretKeyString);
        byte[] keyBytesRefresh = Base64.getDecoder().decode(refreshSecretKeyString);
        secretKeyRefresh = Keys.hmacShaKeyFor(keyBytesRefresh);
        secretKeyAccess = Keys.hmacShaKeyFor(keyBytesAccess);  // Генерируем ключ из строки
    }

    // Генерация токена с UUID userId
    public String generateToken(UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_ACCESS))
                .signWith(secretKeyAccess)
                .compact();
    }

    public String hashToken(String token) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash = digest.digest(token.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    // Получение userId из токена
    public UUID extractUserId(String token, String type) {
        String tokenWithoutBearer = token.substring(7); // Убираем "Bearer " из токена
        Claims claims = getClaimsFromToken(tokenWithoutBearer, type.equals("access") ? secretKeyAccess : secretKeyRefresh);
        String userIdStr = claims.get("userId", String.class);  // Извлекаем userId как строку
        return UUID.fromString(userIdStr);  // Преобразуем строку в UUID
    }

    // Получение времени истечения токена
    public Date extractExpirationTime(String token, String type) {
        Claims claims = getClaimsFromToken(token, type.equals("refresh") ? secretKeyRefresh : secretKeyAccess);
        return claims.getExpiration();
    }

    // Получение всех claims (данных) из токена
    private Claims getClaimsFromToken(String token, SecretKey secretKey) {
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
