package com.sustech.so_java_stats.controller;

import com.sustech.so_java_stats.dto.MultithreadingPitfallResponseDto;
import com.sustech.so_java_stats.dto.QuestionSolvabilityResponseDto;
import com.sustech.so_java_stats.dto.TopicCooccurrenceResponseDto;
import com.sustech.so_java_stats.dto.TopicTrendResponseDto;
import com.sustech.so_java_stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "StatsController", description = "Controller for stats analytics.")
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "Get Topic Trends", description = "Retrieve trends for specific topics over a date range.")
    @GetMapping("/topic-trends")
    public ResponseEntity<TopicTrendResponseDto> getTopicTrends(
            @Parameter(description = "Comma-separated list of topics", example = "stream,collections,multithreading,generics,reflection,lambda")
            @RequestParam String topics,

            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2024-12-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "End date (YYYY-MM-DD)", example = "2025-11-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Data granularity", example = "monthly")
            @RequestParam String granularity
    ) {
        String cleanedTopics = topics.endsWith(",") ? topics.substring(0, topics.length() - 1) : topics;
        List<String> requestedTopics = Arrays.asList(cleanedTopics.split(","));
        TopicTrendResponseDto response = statsService.getTopicTrends(requestedTopics, startDate, endDate, granularity);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Topic Co-occurrences", description = "Find which topics frequently appear together.")
    @GetMapping("/topic-cooccurrences")
    public ResponseEntity<List<TopicCooccurrenceResponseDto>> getTopicCooccurrences(
            @Parameter(description = "Number of top results to return", example = "10")
            @RequestParam int topN,

            @Parameter(description = "Minimum frequency to be considered", example = "10")
            @RequestParam int minFrequency,

            @Parameter(description = "Tags to exclude (comma separated)")
            @RequestParam(required = false) String excludeTags
    ) {
        List<String> excludedTags = (excludeTags != null && !excludeTags.isEmpty())
                ? Arrays.asList(excludeTags.split(","))
                : Collections.emptyList();
        List<TopicCooccurrenceResponseDto> response = statsService.getTopicCooccurrences(topN, minFrequency, excludedTags);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Common Multithreading Pitfalls", description = "Analyzes threads tagged with concurrency topics to find common pitfalls.")
    @GetMapping("/multithreading-pitfalls")
    public ResponseEntity<List<MultithreadingPitfallResponseDto>> getMultithreadingPitfalls(
            @Parameter(description = "Number of top pitfalls to return", example = "7")
            @RequestParam int topN
    ) {
        List<MultithreadingPitfallResponseDto> response = statsService.getMultithreadingPitfalls(topN);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Factors of What Make a Question Solvable or Hard-to-Solve", description = "Compares solvable vs. hard-to-solve questions based on user reputation, question clarity, and number of question upvotes.")
    @GetMapping("/question-solvability")
    public ResponseEntity<QuestionSolvabilityResponseDto> getQuestionSolvabilityFactors() {
        QuestionSolvabilityResponseDto response = statsService.getQuestionSolvabilityFactors();
        return ResponseEntity.ok(response);
    }
}
