package multisensory.project.model;

import java.util.UUID;

public class MetricData {

    private String type;  // Тип метрики: steps, coordinates, heartbeat
    private Integer steps;    // Количество шагов
    private String coordinates; // Координаты (например, "lat=50.45,lon=30.52")
    private Integer heartbeat; // Сердцебиение
    private long timestamp; // Временная метка
    private UUID userId;  // Идентификатор пользователя в формате UUID

    // Преобразование данных в формат Line Protocol для InfluxDB
    public String toLineProtocol() {
        StringBuilder lineProtocol = new StringBuilder();

        // Указываем название измерения (например, "metric")
        lineProtocol.append("metric");

        // Добавляем теги. user_id - это тег.
        lineProtocol.append(",user_id=").append(userId);

        // Добавляем поля. Можно добавить шаги, координаты, сердцебиение и другие метрики.
        if (steps != null) {
            lineProtocol.append(" steps=").append(steps);
        }
        if (coordinates != null) {
            // Координаты передаем в строковом виде с кавычками
            lineProtocol.append(" coordinates=\"").append(coordinates).append("\"");
        }
        if (heartbeat != null) {
            lineProtocol.append(" heartbeat=").append(heartbeat);
        }

        // Добавляем метку времени
        lineProtocol.append(" ").append(timestamp);

        return lineProtocol.toString();
    }

    // Геттеры и сеттеры
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public Integer getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Integer heartbeat) {
        this.heartbeat = heartbeat;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}


