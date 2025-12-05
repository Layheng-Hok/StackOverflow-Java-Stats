package com.sustech.so_java_stats.controller;

import com.sustech.so_java_stats.dto.TopicCooccurrenceResponseDto;
import com.sustech.so_java_stats.dto.TopicTrendResponseDto;
import com.sustech.so_java_stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/topic-trends")
    public ResponseEntity<TopicTrendResponseDto> getTopicTrends(
            @RequestParam String topics,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "yearly") String granularity
    ) {
        TopicTrendResponseDto response = statsService.getTopicTrends(Arrays.asList(topics.split(",")), startDate, endDate, granularity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/topic-cooccurrences")
    public ResponseEntity<List<TopicCooccurrenceResponseDto>> getTopicCooccurrences(
            @RequestParam(defaultValue = "10") int topN,
            @RequestParam(defaultValue = "10") int minFrequency
    ) {
        List<TopicCooccurrenceResponseDto> response = statsService.getTopicCooccurrences(topN, minFrequency);
        return ResponseEntity.ok(response);
    }
}