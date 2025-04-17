package multisensory.project.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import multisensory.project.model.MetricData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MetricService {

    @Value("${influx.url}")
    private String influxdbUrl;
    @Value("${influx.queryUrl}")
    private String influxdbQueryUrl;
    @Value("${influx.authToken}")
    private String authToken;
    @Value("${influx.org}")
    private String org;
    @Value("${influx.bucket}")
    private String bucket;

    private final JwtTokenService jwtTokenService;
    private final WebClient webClient;

    public void saveUserMetric(String token, String type,
                                 String value, long timestamp) {

        UUID userId = jwtTokenService.extractUserId(token, "access");

        MetricData metricData = new MetricData();
        metricData.setType(type);
        metricData.setTimestamp(timestamp);
        metricData.setUserId(userId);

        switch (type) {
            case "steps":
                metricData.setSteps(Integer.parseInt(value));
                break;
            case "coordinates":
                metricData.setCoordinates(value);
                break;
            case "heartbeat":
                metricData.setHeartbeat(Integer.parseInt(value));
                break;
        }

        String data = metricData.toLineProtocol();
        System.out.println("Data to be sent to InfluxDB: " + data);
        String url = influxdbUrl + "?bucket=" + bucket + "&org=" + org + "&precision=s";

        try {
            String result = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Token " + authToken)
                    .contentType(MediaType.TEXT_PLAIN)
                    .bodyValue(data)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Error with saving metric");
        }
    }

    public String getUserMetrics(String token, String start,
                                             String stop, String metricType) {

        UUID userId = jwtTokenService.extractUserId(token, "access");
        String url = influxdbQueryUrl + "?org=" + org;
        System.out.println(url);


        String query = String.format(
                "{\"query\": \"from(bucket: \\\"%s\\\") " +
                        "|> range(start: %s, stop: %s) " +  // Временной диапазон
                        "|> filter(fn: (r) => r._field == \\\"%s\\\" and r.user_id == \\\"%s\\\") " +  // Фильтр по метрике и userId
                        "|> sort(columns: [\\\"_time\\\"], desc: false)\"}",  // Сортировка по времени
                bucket, start, stop, metricType, userId.toString()
        );

        try {
            String response = webClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Token " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(query)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null) {
                return getValuesFromResponse(response);
            }

            throw new EntityNotFoundException("Metric not found");

        } catch (WebClientResponseException ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }
    }

    private String getValuesFromResponse(String response) {
        String[] lines = response.split("\n");

        List<String> values = new ArrayList<>();

        for (String line : lines) {
            if (!line.contains("_value")) {
                String[] columns = line.split(",");
                if (columns.length >= 3) {
                    values.add(columns[6]);
                }
            }
        }

        return String.join(", " , values);
    }
}

