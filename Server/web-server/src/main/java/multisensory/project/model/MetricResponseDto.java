package multisensory.project.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class MetricResponseDto {
    private Instant timestamp;
    private Object value;
}
