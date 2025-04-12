package multisensory.project.service;

import multisensory.project.model.RefreshToken;
import multisensory.project.repository.RefreshTokenRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    JwtTokenService jwtTokenService;
    @Mock
    RefreshTokenRepository refreshTokenRepository;
    @InjectMocks
    RefreshTokenService refreshTokenService;

    @Test
    public void RefreshTokenService_RefreshToken_ReturnJwtToken() {
        RefreshToken token = new RefreshToken();
        token.setRefreshToken("Token");
        token.setExpirationTime(new Timestamp(System.currentTimeMillis() + 1000));

        when(jwtTokenService.extractUserId(Mockito.anyString(), eq("refresh")))
                .thenReturn(UUID.randomUUID());
        when(jwtTokenService.generateToken(Mockito.any(UUID.class)))
                .thenReturn("GeneratedToken");
        when(jwtTokenService.hashToken(Mockito.anyString()))
                .thenReturn("Token");
        when(refreshTokenRepository.findById(Mockito.any(UUID.class)))
                .thenReturn(Optional.of(token));

        Assertions.assertThat(refreshTokenService.refreshToken("Bearer Token"))
                .isEqualTo("GeneratedToken");
    }

    @Test
    public void RefreshTokenService_GenerateRefreshToken_ReturnNewToken() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshSecretKeyString",
                "eWmK6jiP7hsas9vjgnmZv1fNl30sLJSfDO9RhZvnDT9s9wipjffANGRg6kFvLs1k");
        refreshTokenService.init();

        Assertions.assertThat(refreshTokenService.generateRefreshToken(UUID.randomUUID()))
                .isNotNull();
        Assertions.assertThat(refreshTokenService.generateRefreshToken(UUID.randomUUID())
                .length()).isGreaterThan(0);
    }
}
