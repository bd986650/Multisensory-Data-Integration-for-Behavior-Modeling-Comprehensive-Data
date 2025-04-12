package multisensory.project.controller;

import multisensory.project.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveMetric(
            @RequestHeader("Authorization") String token,
            @RequestParam String type,
            @RequestParam String value,
            @RequestParam long timestamp) {
        metricService.saveUserMetric(token, type, value, timestamp);
        return ResponseEntity.ok("Metric saved");
    }

    @GetMapping("/get")
    public ResponseEntity<String> getMetrics(
            @RequestHeader("Authorization") String token,
            @RequestParam String start,
            @RequestParam String stop,
            @RequestParam String metricType) {
        String response  = metricService.getUserMetrics(token, start, stop, metricType);
        return ResponseEntity.ok(response);
    }
}
