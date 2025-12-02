package com.sustech.so_java_stats.controller;

import com.sustech.so_java_stats.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Tag(name = "Stats API", description = "API for retrieving statistics on Stack Overflow Java topics")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @Operation(summary = "Retrieve topic trends", description = "Fetches the number of questions per topic over time, grouped by the specified granularity.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of topic trends",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(value = "{\"topics\": [\"generics\", \"collections\"], \"data\": {\"generics\": [{\"date\": \"2022-01\", \"value\": 10}, {\"date\": \"2022-02\", \"value\": 15}], \"collections\": [{\"date\": \"2023-01\", \"value\": 20}, {\"date\": \"2022-02\", \"value\": 25}]}}"))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters supplied",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"At least one topic is required\"}")))
    })
    @GetMapping("/topic-trends")
    public ResponseEntity<Map<String, Object>> getTopicTrends(
            @Parameter(description = "Comma-separated list of topics (tags) to retrieve trends for", required = true, example = "generics,collections,lambda")
            @RequestParam String topics,
            @Parameter(description = "Start date for the trend period in YYYY-MM-DD format", required = true, example = "2022-01-01")
            @RequestParam String startDate,
            @Parameter(description = "End date for the trend period in YYYY-MM-DD format", required = true, example = "2025-12-31")
            @RequestParam String endDate,
            @Parameter(description = "Granularity of the trend buckets: 'monthly' or 'yearly'", example = "monthly")
            @RequestParam(defaultValue = "monthly") String granularity) {

        // Parse and validate topics
        List<String> topicList = Arrays.stream(topics.split(","))
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
