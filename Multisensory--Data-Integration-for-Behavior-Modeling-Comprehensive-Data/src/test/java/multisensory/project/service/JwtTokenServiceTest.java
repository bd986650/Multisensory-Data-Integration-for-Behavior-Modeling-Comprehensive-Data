package multisensory.project.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class JwtTokenServiceTest {
    JwtTokenService jwtTokenService;

    @BeforeEach
    public void setUp() {
        jwtTokenService = new JwtTokenService();

        ReflectionTestUtils.setField(jwtTokenService, "refreshSecretKeyString",
                "eWmK6jiP7hsas9vjgnmZv1fNl30sLJSfDO9RhZvnDT9s9wipjffANGRg6kFvLs1k");
        ReflectionTestUtils.setField(jwtTokenService, "accessSecretKeyString",
                "eWmK6jiP7hsas9vjgnmZv1fNl30sLJSfDO9RhZvnDT9s9vopjffANGRg6kFvLs1k");

        jwtTokenService.init();
    }

    @Test
    public void JwtTokenService_GenerateToken_ReturnToken() {
        Assertions.assertThat(jwtTokenService.generateToken(UUID.randomUUID()))
                .isNotNull();
        Assertions.assertThat(jwtTokenService.generateToken(UUID.randomUUID()).length())
                .isGreaterThan(0);
    }

    @Test
    public void JwtTokenService_HashToken_ReturnHashedToken() {
        String token = "token";

        Assertions.assertThat(jwtTokenService.hashToken(token))
                .isNotNull();
        Assertions.assertThat(jwtTokenService.hashToken(token).length())
                .isGreaterThan(0);
    }

    @Test
    public void JwtTokenService_ExtractUserId_ReturnUUID() {
        String token = "Bearer " + jwtTokenService.generateToken(UUID.randomUUID());
        String type = "access";

        Assertions.assertThat(jwtTokenService.extractUserId(token,type))
                .isNotNull();
    }

    @Test
    public void JwtTokenService_ExtractExpirationTime_ReturnExpirationTime() {
        String token = jwtTokenService.generateToken(UUID.randomUUID());
        String type = "access";

        Assertions.assertThat(jwtTokenService.extractExpirationTime(token,type))
                .isNotNull();
    }
}
