package multisensory.project.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import multisensory.project.model.AuthTokensDto;
import multisensory.project.model.RefreshToken;
import multisensory.project.model.User;
import multisensory.project.repository.UserRepository;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Service
public class UserService {

    UserRepository userRepository;
    RefreshTokenService refreshTokenService;
    JwtTokenService jwtTokenService;

    public UserService(UserRepository userRepository,
                       RefreshTokenService refreshTokenService,
                       JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.jwtTokenService = jwtTokenService;
    }

    public UUID generateUUID() {
        return UUID.randomUUID();
    }

    public String userRegister(String name, String hashPassword) {
        System.out.println("Register attempt");
        if (existsByUsername(name)) {
            throw new EntityExistsException("Username already exists");
        }

        UUID userId = generateUUID();

        // Сохранение данных пользователя (user_id, username, password) в таблицу
        saveUser(new User(userId, name, hashPassword));

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

    public User findByName(String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("User with this name does not exist"));
    }

    public boolean existsByUsername(String name) {
        return userRepository.findByName(name).isPresent();
    }

    public void saveUser(User user) {
        try {
            userRepository.save(user);
        } catch (DataAccessResourceFailureException e) {
            throw new DataAccessResourceFailureException("DataBase connection Error");
        }
    }
}
