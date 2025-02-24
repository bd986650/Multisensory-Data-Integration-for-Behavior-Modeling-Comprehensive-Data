package multisensory.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

        // Проверяем действителен ли токен
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).body("Token has expired, please refresh your token");
        }

        // Извлекаем userId из токена
        UUID userId = jwtTokenUtil.extractUserId(token); // Извлекаем userId
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

    // Новый метод для получения данных по JWT и таймштампу
    @GetMapping("/get-data")
    public ResponseEntity<?> getData(
            @RequestHeader("Authorization") String token,  // JWT токен
            @RequestParam Long timestamp) {  // Таймштамп

        // Проверяем действителен ли токен
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(401).body("Token has expired, please refresh your token");
        }

        // Извлекаем userId из токена
        UUID userId = jwtTokenUtil.extractUserId(token); // Извлекаем userId
        System.out.println("uid: " + userId);

        // Преобразуем timestamp в LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC);

        try {
            // Поиск данных в таблице data_records по userId и timestamp
            String dataRecords = userRepository.findDataByUserIdAndExactMinute(userId, dateTime);

            if (dataRecords.isEmpty()) {
                return ResponseEntity.status(404).body("No data found for this user and timestamp");
            }

            return ResponseEntity.ok(dataRecords);  // Возвращаем найденные данные

        } catch (Exception e) {
            System.out.println("Error during database operation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error during database operation");
        }
    }
}
