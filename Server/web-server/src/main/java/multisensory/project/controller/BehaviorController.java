package multisensory.project.controller;

import lombok.RequiredArgsConstructor;
import multisensory.project.service.BehaviorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/behavior")
@RequiredArgsConstructor
public class BehaviorController {

    private final BehaviorService behaviorService;

    @GetMapping("/activity")
    public int getUserActivityStatus(
            @RequestHeader("Authorization") String token,
            @RequestParam("timestamp") String timestamp) {

        return behaviorService.analyzeUserBehavior(
                token,
                timestamp,
                timestamp
        );
    }
}
