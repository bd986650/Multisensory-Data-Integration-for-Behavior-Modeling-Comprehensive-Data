package multisensory.project.service;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import multisensory.project.model.RefreshToken;
import multisensory.project.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;

    @Value("${refresh.jwt.secret}")
    private String refreshSecretKeyString;
    private SecretKey secretKeyRefresh; // ключ для подписи refresh токена
    private final long EXPIRATION_TIME_REFRESH = 2629744000L; // Время жизни рефреш токена (1 мес)

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               JwtTokenService jwtTokenService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @PostConstruct
    public void init() {
        byte[] keyBytesRefresh = Base64.getDecoder().decode(refreshSecretKeyString);
        secretKeyRefresh = Keys.hmacShaKeyFor(keyBytesRefresh);
    }

    public RefreshToken getRefreshTokenByUserId(UUID userId) {
        return refreshTokenRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("Refresh token is not found"));
    }

    private void deleteRefreshToken(UUID userId) {
        refreshTokenRepository.deleteById(userId);
    }

    public String refreshToken(String refreshToken) {
        UUID userId = jwtTokenService.extractUserId(refreshToken, "refresh");
        RefreshToken refreshTokenData = getRefreshTokenByUserId(userId);
        String refreshWithOutBearer = refreshToken.substring(7);
        String hashedRefreshToken = jwtTokenService.hashToken(refreshWithOutBearer);

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
        Optional<RefreshToken> refreshExists = refreshTokenRepository.findById(refreshToken.getUserId());
        if (refreshExists.isPresent()) {
            deleteRefreshToken(refreshToken.getUserId());
        }
        refreshTokenRepository.save(refreshToken);
    }
}
