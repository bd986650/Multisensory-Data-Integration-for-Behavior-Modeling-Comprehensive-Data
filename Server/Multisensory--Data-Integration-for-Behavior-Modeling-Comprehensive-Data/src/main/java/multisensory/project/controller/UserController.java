package multisensory.project.controller;

import io.jsonwebtoken.ExpiredJwtException;
import multisensory.project.model.MetricData;
import multisensory.project.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    JwtTokenService jwtTokenService;

    private static final String INFLUXDB_URL = "http://localhost:8086/api/v2/write";
    private static final String INFLUXDB_QUERY_URL = "http://localhost:8086/api/v2/query";
    private static final String AUTH_TOKEN = "fN0LMtZOBct_OEjMrYDc2_POBLJcDMPvBle" +
            "_e7sNx1nwlw4lDDmak0iJ3Ceer204zC9aPSwOM_TPppvLZ9qqCA=="; // Ваш токен InfluxDB
    private static final String ORG = "MultiSens";  // Организация в InfluxDB
    private static final String BUCKET = "MultiSens";  // Ваш бакет InfluxDB

    private final RestTemplate restTemplate = new RestTemplate();

    // POST запрос для сохранения метрики (шаги, координаты, сердцебиение)
    @PostMapping("/save")
    public ResponseEntity<String> saveMetric(
            @RequestHeader("Authorization") String token,  // Токен авторизации
            @RequestParam String type,
            @RequestParam String value,
            @RequestParam long timestamp) {

        // Проверка истечения срока действия токена
        if (jwtTokenService.isTokenExpired(token)) {
            return ResponseEntity.status(401).body("Token has expired");
        }

        // Извлекаем userId из токена
        UUID userId = jwtTokenService.extractUserId(token, "access");

        // Формируем данные для сохранения
        MetricData metricData = new MetricData();
        metricData.setType(type);
        metricData.setTimestamp(timestamp);
        metricData.setUserId(userId);

        // Определяем значение метрики в зависимости от типа
        if ("steps".equals(type)) {
            metricData.setSteps(Integer.parseInt(value));  // Если шаги, передаем как integer
        } else if ("coordinates".equals(type)) {
            metricData.setCoordinates(value);  // Если координаты, передаем как строку
        } else if ("heartbeat".equals(type)) {
            metricData.setHeartbeat(Integer.parseInt(value));  // Если сердцебиение, передаем как integer
        }

        // Преобразуем данные в формат Line Protocol
        String data = metricData.toLineProtocol();

        // Логирование данных перед отправкой
        System.out.println("Data to be sent to InfluxDB: " + data);

        // Формируем URL для записи данных в InfluxDB
        String url = INFLUXDB_URL + "?bucket=" + BUCKET + "&org=" + ORG + "&precision=s";
        System.out.println("InfluxDB URL: " + url);

        // Создаем заголовки с авторизацией
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);  // Передаем токен в формате Bearer
        headers.setContentType(MediaType.TEXT_PLAIN);  // Убедитесь, что тип контента правильный

        // Создаем тело запроса
        HttpEntity<String> entity = new HttpEntity<>(data, headers);

        // Отправляем запрос в InfluxDB
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Логирование ответа
            System.out.println("Response from InfluxDB: " + response.getStatusCode() + " " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                return new ResponseEntity<>("Metric saved", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Failed to save metric", response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Ловим ошибки клиента и сервера, чтобы вывести более подробное сообщение
            System.err.println("Error saving metric: " + ex.getResponseBodyAsString());
            return new ResponseEntity<>("Error saving metric: " + ex.getResponseBodyAsString(), ex.getStatusCode());
        } catch (Exception e) {
            // Ловим все другие возможные ошибки
            System.err.println("Unexpected error: " + e.getMessage());
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // GET запрос для получения метрик
    @GetMapping("/get")
    public ResponseEntity<String> getMetrics(
            @RequestHeader("Authorization") String token,
            @RequestParam String start,
            @RequestParam String stop,
            @RequestParam String metricType) {  // Получаем userId как параметр запроса

        if (jwtTokenService.isTokenExpired(token)) {
            return ResponseEntity.status(401).body("Token has expired");
        }

        UUID userId = jwtTokenService.extractUserId(token, "access");  // Преобразуем строку в UUID

        String url = INFLUXDB_QUERY_URL + "?org=" + ORG;

        // Строим Flux query для получения данных по userId и метрике
        String query = String.format(
                "{\"query\": \"from(bucket: \\\"%s\\\") " +
                        "|> range(start: %s, stop: %s) " +  // Временной диапазон
                        "|> filter(fn: (r) => r._field == \\\"%s\\\" and r.user_id == \\\"%s\\\") " +  // Фильтр по метрике и userId
                        "|> sort(columns: [\\\"_time\\\"], desc: false)\"}",  // Сортировка по времени
                BUCKET, start, stop, metricType, userId.toString()
        );

        System.out.println(metricType);
        System.out.println("InfluxDB Query: " + query);


        // Создаем заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + AUTH_TOKEN);  // Используем формат Bearer для авторизации
        headers.setContentType(MediaType.APPLICATION_JSON);
        // Создаем тело запроса
        HttpEntity<String> entity = new HttpEntity<>(query, headers);

        // Отправляем запрос в InfluxDB
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // Извлекаем тело ответа
            String responseBody = response.getBody();

            // Если ответ не пустой, пытаемся извлечь все значения
            if (responseBody != null) {
                // Разделяем строки ответа
                String[] lines = responseBody.split("\n");

                // Список для хранения всех значений
                List<String> values = new ArrayList<>();

                for (String line : lines) {
                    // Пропускаем строки с заголовками, проверяем на наличие значений
                    if (!line.contains("_value")) {
                        String[] columns = line.split(",");
                        if (columns.length >= 3) {
                            values.add(columns[6]);  // Третий столбец содержит _value
                        }
                    }
                }

                if (!values.isEmpty()) {
                    // Преобразуем список значений в строку, разделенную запятыми, или можно выбрать другой формат
                    String allValues = String.join(", ", values);
                    return new ResponseEntity<>(allValues, response.getStatusCode());
                }
            }

            return new ResponseEntity<>("No data found", HttpStatus.NOT_FOUND);

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Ловим ошибки клиента и сервера, чтобы вывести более подробное сообщение
            return new ResponseEntity<>("Error: " + ex.getResponseBodyAsString(), ex.getStatusCode());
        } catch (Exception e) {
            // Ловим другие возможные ошибки
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<String> getAllMetrics(
            @RequestHeader("Authorization") String token,
            @RequestParam String start,
            @RequestParam String stop) {

        // Проверяем, не истек ли токен
        if (jwtTokenService.isTokenExpired(token)) {
            return ResponseEntity.status(401).body("Token has expired");
        }

        // Формируем URL для запроса данных
        String url = INFLUXDB_QUERY_URL + "?org=" + ORG;

        // Строим Flux query для получения всех данных в бакете за определенный временной диапазон
        String query = String.format(
                "{\"query\": \"from(bucket: \\\"%s\\\") |> range(start: %s, stop: %s)\"}",
                BUCKET, start, stop
        );

        // Создаем заголовки
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Token " + AUTH_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Создаем тело запроса
        HttpEntity<String> entity = new HttpEntity<>(query, headers);

        // Отправляем запрос в InfluxDB
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return new ResponseEntity<>(response.getBody(), response.getStatusCode());
    }


}

