package com.neobank.ui.controller;

import com.neobank.ui.stats.StatsAggregator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StatsController {

    private final StatsAggregator statsAggregator;

    @GetMapping
    public ResponseEntity<String> getStats() {
        return ResponseEntity.ok(statsAggregator.getCurrentStatsJson());
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("{\"status\": \"alive\", \"service\": \"ui-backend\"}");
    }
}