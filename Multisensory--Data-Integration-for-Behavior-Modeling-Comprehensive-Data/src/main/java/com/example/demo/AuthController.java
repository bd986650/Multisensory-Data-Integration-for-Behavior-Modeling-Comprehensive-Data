package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Создаем объект для хэширования паролей
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Регистрация нового пользователя
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestParam String username, @RequestParam String password) {
        try {
            // Проверка, существует ли пользователь с таким логином
            if (userRepository.existsByUsername(username)) {
                return ResponseEntity.status(400).body("Username already exists");
            }

            // Хэшируем пароль
            String hashedPassword = passwordEncoder.encode(password);

            // Создание uid
            UUID user_id = userRepository.generateUniqueUserId();

            // Сохранение данных пользователя (user_id, username, password) в таблицу
            userRepository.saveUser(user_id, username, hashedPassword);

            // Генерация и возврат JWT токена
            String token = jwtTokenUtil.generateToken(user_id, username);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            // Логируем и возвращаем сообщение об ошибке
            System.err.println("Error during database operation: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error: " + e.getMessage());
        }
    }


    // авторизация пользователя
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestParam String username, @RequestParam String password) {
        // Ищем данные пользователя по имени пользователя
        UserData userData = userRepository.findUserDataByUsername(username);

        if (userData != null) {
            // Сравниваем введённый пароль с хэшированным в базе данных
            if (passwordEncoder.matches(password, userData.getPassword())) {
                // Генерация JWT токена с userId и username
                String token = jwtTokenUtil.generateToken(userData.getUserId(), username);
                return ResponseEntity.ok(token); // Возвращаем токен клиенту
            }
        }

        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
