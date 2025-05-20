package multisensory.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import multisensory.project.model.AuthTokensDto;
import multisensory.project.model.User;
import multisensory.project.service.AuthService;
import multisensory.project.service.JwtTokenService;
import multisensory.project.service.RefreshTokenService;
import multisensory.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private BCryptPasswordEncoder passwordEncoder;
    @MockBean
    private JwtTokenService jwtTokenService;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
        public void AuthController_RegisterUser_ReturnToken() throws Exception {
        when(passwordEncoder.encode(Mockito.anyString())).thenReturn("Password");
        when(authService.userRegister(Mockito.anyString(), Mockito.anyString())).thenReturn("Token");

        ResultActions response = mockMvc.perform(post("/api/auth/register")
                .param("username", "testuser")
                .param("password", "password")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk()).andExpect(content().string("Token"));
    }

    @Test
    public void AuthController_LoginUser_ReturnBothTokens() throws Exception {
        User user = new User(UUID.randomUUID(), "Alex", "passwd");
        AuthTokensDto tokens = new AuthTokensDto("access", "refresh");

        when(userService.findByName(Mockito.anyString())).thenReturn(user);
        when(authService.userLogin(Mockito.anyString(), Mockito.any(UUID.class))).thenReturn(tokens);
        when(passwordEncoder.matches(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .param("username", "user")
                .param("password", "password")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("access"))
                .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    public void AuthController_RefreshToken_ReturnAccessToken() throws Exception {
        when(refreshTokenService.refreshToken(Mockito.anyString()))
                .thenReturn("Token");

        ResultActions response = mockMvc.perform(post("/api/auth/refresh")
                .header("Authorization", "refreshToken")
                .contentType(MediaType.APPLICATION_JSON));

        response.andExpect(status().isOk())
                .andExpect(content().string("Token"));
    }
}
