package com.sustech.so_java_stats.controller;

import com.sustech.so_java_stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/topic-trends")
    public ResponseEntity<Map<String, Object>> getTopicTrends(
            @RequestParam String topics,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "monthly") String granularity) {

        // Parse and validate topics
        java.util.List<String> topicList = Arrays.stream(topics.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (topicList.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "At least one topic is required"));
        }

        // Validate granularity
        if (!Set.of("monthly", "yearly").contains(granularity)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "granularity must be 'monthly' or 'yearly'"));
        }

        // Parse dates
        LocalDate startLd;
        LocalDate endLd;
        try {
            startLd = LocalDate.parse(startDate);
            endLd = LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid date format. Use ISO format (yyyy-MM-dd)"));
        }

        if (endLd.isBefore(startLd)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "endDate must not be before startDate"));
        }

        Map<String, Object> result = statsService.getTopicTrends(topicList, startLd, endLd, granularity);
        return ResponseEntity.ok(result);
    }
}
