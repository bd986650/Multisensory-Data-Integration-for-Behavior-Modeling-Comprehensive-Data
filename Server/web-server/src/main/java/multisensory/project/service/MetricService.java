package multisensory.project.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import lombok.RequiredArgsConstructor;
import multisensory.project.model.MetricResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetricService {

    @Value("${influx.bucket}")
    private String bucket;

    private final JwtTokenService jwtTokenService;
    private final InfluxDBClient influxDBClient;
    private static final Logger log = LoggerFactory.getLogger(MetricService.class);

    public void saveUserMetric(String token, String type, String value, long timestamp) {
        UUID userId = jwtTokenService.extractUserId(token, "access");

        Point point = Point.measurement("user_metrics")
                .addTag("user_id", userId.toString())
                .addTag("metric_type", type)
                .time(timestamp, WritePrecision.S);

        switch (type) {
            case "steps":
            case "heartbeat":
            case "notification":
            case "coordinates":
                point.addField("value", value);
                break;
            default:
                throw new IllegalArgumentException("Unknown metric type: " + type);
        }

        try {
            influxDBClient.getWriteApi().writePoint(point);
            log.info("Data sent to InfluxDB: {}", point.toLineProtocol());
        } catch (Exception ex) {
            log.error("Error saving metric", ex);
            throw new RuntimeException("Failed to save metric", ex);
        }
    }

    public List<MetricResponseDto> getUserMetrics(String token, String start, String stop, String metricType) {
        UUID userId = jwtTokenService.extractUserId(token, "access");

        String fluxQuery = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: %s, stop: %s) " +
                        "|> filter(fn: (r) => r._field == \"value\" and r.user_id == \"%s\" and r.metric_type == \"%s\") " +
                        "|> sort(columns: [\"_time\"], desc: false)",
                bucket, start, stop, userId, metricType
        );

        List<FluxTable> tables = influxDBClient.getQueryApi().query(fluxQuery);

        log.info("Data has been successfully retrieved from database");

        return tables.stream()
                .flatMap(table -> table.getRecords().stream())
                .map(record -> new MetricResponseDto(
                        record.getTime(),
                        record.getValue()
                ))
                .collect(Collectors.toList());
    }
}
