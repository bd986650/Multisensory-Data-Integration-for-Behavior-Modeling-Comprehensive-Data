package data_analysis;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyze")
public class AnalysisController {

    @PostMapping
    public AnalysisResult analyze(@RequestBody DataPayload payload) {
        boolean isWalking = (payload.getSteps() > 0) || (payload.getHeartRate() > 55);
        AnalysisResult result = new AnalysisResult();
        result.setStatus(isWalking ? 1 : 0);
        return result;
    }
}