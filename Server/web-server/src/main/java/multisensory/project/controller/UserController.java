package multisensory.project.controller;

import multisensory.project.model.AuthTokensDto;
import multisensory.project.model.User;
import multisensory.project.service.JwtTokenService;
import multisensory.project.service.RefreshTokenService;
import multisensory.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private JwtTokenService jwtTokenUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Регистрация нового пользователя
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam String username, @RequestParam String password) {
        String hashedPassword = passwordEncoder.encode(password);
        String token = userService.userRegister(username, hashedPassword);
        return ResponseEntity.ok(token);
    }

    // авторизация пользователя
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
        User user = userService.findByName(username);
        String userPassword = user.getPassword();
        if (passwordEncoder.matches(password, userPassword)) {
            AuthTokensDto tokens = userService.userLogin(username, user.getUserId());
            return ResponseEntity.ok(tokens);
        }
        throw new BadCredentialsException("Invalid Credentials");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        String accessToken = refreshTokenService.refreshToken(token);
        return ResponseEntity.ok(accessToken);
    }
}
