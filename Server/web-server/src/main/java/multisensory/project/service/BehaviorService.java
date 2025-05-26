package multisensory.project.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorService {

    private final InfluxDBClient influxDBClient;
    private final JwtTokenService jwtTokenService;
    private final RestTemplate restTemplate;

    @Value("${influx.bucket}")
    private String bucket;

    @Value("${analysis.service.url}")
    private String analysisServiceUrl;

    public Integer analyzeUserBehavior(String token, String start, String stop) {
        UUID userId = jwtTokenService.extractUserId(token, "access");

        Integer steps = getMetricValue(userId, "steps", start, stop);
        Integer heartRate = getMetricValue(userId, "heartbeat", start, stop);

        Map<String, Object> requestData = new HashMap<>();
        requestData.put("steps", steps);
        requestData.put("heartRate", heartRate);

        return sendToAnalysisService(requestData);
    }

    private Integer getMetricValue(UUID userId, String metricType, String start, String stop) {
        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._field == \"value\" and r.user_id == \"%s\" and r.metric_type == \"%s\") " +
                        "|> last()",
                bucket, start, stop, userId, metricType
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery);

        if (tables.isEmpty() || tables.get(0).getRecords().isEmpty()) {
            return 0;
        }

        return Integer.parseInt(tables.get(0).getRecords().get(0).getValue().toString());
    }

    private Integer sendToAnalysisService(Map<String, Object> requestData) {
        try {
            return restTemplate.postForObject(
                    analysisServiceUrl,
                    requestData,
                    Integer.class
            );
        } catch (Exception ex) {
            log.error("Failed to send data to analysis service", ex);
            throw new RuntimeException("Analysis service error", ex);
        }
    }
}