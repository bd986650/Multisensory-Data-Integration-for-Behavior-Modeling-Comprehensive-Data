package com.example.demo;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Генерация уникального ID для пользователя
    public UUID generateUniqueUserId() {
        return UUID.randomUUID(); // Генерируем уникальный UUID
    }

    // Метод для сохранения данных о пользователе (id, username, password) в таблицу user_data
    public void saveUser(UUID userId, String username, String password) {
        try {
            String sql = "INSERT INTO user_data (user_id, username, password) VALUES (?, ?, ?)";
            System.out.println("Executing SQL: " + sql + " with parameters: " + userId + ", " + username + ", " + password);
            jdbcTemplate.update(sql, userId, username, password);
        } catch (Exception e) {
            System.err.println("Error during database operation: " + e.getMessage());
        }

    }

    public String findDataByUserIdAndExactMinute(UUID userId, LocalDateTime startOfMinute) {
        // Преобразуем LocalDateTime в Unix timestamp (секунды)
        long timestampInSeconds = startOfMinute.toEpochSecond(ZoneOffset.UTC);

        // Запрос в ClickHouse для поиска записи по user_id и времени, округленному до начала минуты
        String sql = "SELECT json_data FROM test_db.data_records " +
                "WHERE user_id = ? " +
                "AND toStartOfMinute(timestamp) = toStartOfMinute(toDateTime(?))";

        try {
            // Выполнение запроса с параметризацией
            return jdbcTemplate.queryForObject(sql, String.class, userId, timestampInSeconds);
        } catch (EmptyResultDataAccessException e) {
            // Если данных нет, возвращаем пустую строку
            return "";
        }
    }



    // Метод для получения user_id и пароля по имени пользователя
    public UserData findUserDataByUsername(String username) {
        String sql = "SELECT user_id, password FROM user_data WHERE username = ?";

        try {
            // Извлекаем user_id и password из базы данных
            return jdbcTemplate.queryForObject(sql, new Object[]{username}, (rs, rowNum) -> {
                // Возвращаем объект, который содержит и user_id, и пароль
                UUID userId = UUID.fromString(rs.getString("user_id"));
                String password = rs.getString("password");
                return new UserData(userId, password);
            });
        } catch (EmptyResultDataAccessException e) {
            // Если пользователь не найден, возвращаем null
            return null;
        }
    }

    // Метод для проверки, существует ли пользователь с таким именем
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM user_data WHERE username = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{username}, Integer.class);
        return count != null && count > 0;
    }

    // Метод для добавления записей: таймштамп - json_data - user_id
    public void saveData(String jsonData, UUID userId) {
        // Генерация Unix timestamp в секундах
        long timestampInMillis = System.currentTimeMillis();
        long timestamp = timestampInMillis / 1000;  // Конвертируем в секунды

        // Параметризованный SQL-запрос
        String sql = "INSERT INTO data_records (user_id, timestamp, json_data) VALUES (?, ?, ?)";

        // Выполнение запроса с параметризацией
        jdbcTemplate.update(sql, userId, timestamp, jsonData);  // Передаем Unix timestamp в секундах
    }


}
