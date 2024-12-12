package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/save-data")
    public ResponseEntity<?> saveData(
            @RequestHeader("Authorization") String token,  // JWT токен
            @RequestBody String jsonData) { // JSON данные напрямую как строка

        // Извлекаем userId из токена
        String tokenWithoutBearer = token.substring(7); // Убираем "Bearer " из токена
        UUID userId = jwtTokenUtil.extractUserId(tokenWithoutBearer); // Извлекаем userId
        System.out.println("uid: " + userId);


        try {
            // Сохранение данных в базу данных (таймштамп и json)
            userRepository.saveData(jsonData, userId);
            System.out.println("Data saved to database");
        } catch (Exception e) {
            System.out.println("Error during database operation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error during database operation");
        }

        return ResponseEntity.ok("Data saved successfully");
    }

    /*/ Удалить пользователя
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
     */
}
