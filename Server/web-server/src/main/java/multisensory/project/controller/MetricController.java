package multisensory.project.controller;

import multisensory.project.model.MetricRequestDto;
import multisensory.project.model.MetricResponseDto;
import multisensory.project.service.MetricService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @RequestBody MetricRequestDto request) {
        metricService.saveUserMetric(token, request.getType(),
                request.getValue(), request.getTimestamp());
        return ResponseEntity.ok("Metric saved");
    }

    @GetMapping("/get")
    public ResponseEntity<List<MetricResponseDto>> getMetrics(
            @RequestHeader("Authorization") String token,
            @RequestParam String start,
            @RequestParam String stop,
            @RequestParam String metricType) {
        List<MetricResponseDto> response  = metricService.getUserMetrics(token, start, stop, metricType);
        return ResponseEntity.ok(response);
    }
}
