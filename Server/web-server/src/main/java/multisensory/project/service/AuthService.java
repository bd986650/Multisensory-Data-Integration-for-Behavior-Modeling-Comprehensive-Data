package multisensory.project.service;

import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import multisensory.project.model.AuthTokensDto;
import multisensory.project.model.RefreshToken;
import multisensory.project.model.User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public String userRegister(String name, String hashPassword) {
        if (userService.existsByUsername(name)) {
            throw new EntityExistsException("Username already exists");
        }

        UUID userId = userService.generateUUID();

        userService.saveUser(new User(userId, name, hashPassword));

        return jwtTokenService.generateToken(userId);
    }

    public AuthTokensDto userLogin(String name, UUID userId ) {
        String token = jwtTokenService.generateToken(userId);
        String refreshToken = refreshTokenService.generateRefreshToken(userId);
        String hashedRefresh = jwtTokenService.hashToken(refreshToken);
        Date expiresAt = jwtTokenService.extractExpirationTime(refreshToken,"refresh");
        refreshTokenService.saveRefreshToken(new RefreshToken(userId, hashedRefresh, new Timestamp(expiresAt.getTime())));
        return new AuthTokensDto(token, refreshToken);
    }
}
