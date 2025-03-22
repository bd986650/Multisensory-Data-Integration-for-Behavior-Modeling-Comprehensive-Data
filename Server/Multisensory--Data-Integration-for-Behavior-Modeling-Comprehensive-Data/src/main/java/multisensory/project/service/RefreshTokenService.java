package multisensory.project.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import multisensory.project.model.RefreshToken;
import multisensory.project.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class RefreshTokenService {
    RefreshTokenRepository refreshTokenRepository;
    JwtTokenService jwtTokenService;

    @Value("${refresh.jwt.secret}")
    private String refreshSecretKeyString;
    private SecretKey secretKeyRefresh; // ключ для подписи refresh токена
    private final long EXPIRATION_TIME_REFRESH = 2629744000L; // Время жизни рефреш токена (1 мес)

    @PostConstruct
    public void init() {
        byte[] keyBytesRefresh = Base64.getDecoder().decode(refreshSecretKeyString);
        secretKeyRefresh = Keys.hmacShaKeyFor(keyBytesRefresh);
    }

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtTokenService jwtTokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public RefreshToken getRefreshTokenByUserId(UUID userId) {
        return refreshTokenRepository.findByUserId(userId).orElseThrow(() -> new NoSuchElementException("Refresh token is not found"));
    }

    private void deleteRefreshToken(UUID userId) {
        refreshTokenRepository.deleteById(userId);
    }

    public String refreshToken(String refreshToken) {
        UUID userId = jwtTokenService.extractUserId(refreshToken, "refresh");
        RefreshToken refreshTokenData = getRefreshTokenByUserId(userId);
        String hashedRefreshToken;
        String refreshWithOutBearer = refreshToken.substring(7);
        hashedRefreshToken = jwtTokenService.hashToken(refreshWithOutBearer);

        if (refreshTokenData.getRefreshToken().equals(hashedRefreshToken)) {
            if (!refreshTokenData.getExpirationTime().before(new Date())) {
                return jwtTokenService.generateToken(userId);
            }
            throw new ExpiredJwtException(null, null, "Refresh token has expired");
        }
        throw new JwtException("Invalid refresh token");
    }

    public String generateRefreshToken(UUID userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.toString());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_REFRESH))  // Долгий срок действия
                .signWith(secretKeyRefresh)  // Используем другой ключ для подписи refresh токена
                .compact();
    }

    public void saveRefreshToken(RefreshToken refreshToken) {
        Optional<RefreshToken> refreshExists = refreshTokenRepository.findByUserId(refreshToken.getUserId());
        if (refreshExists.isPresent()) {
            deleteRefreshToken(refreshToken.getUserId());
        }
        refreshTokenRepository.save(refreshToken);
    }
}
