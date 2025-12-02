package com.sustech.so_java_stats.service;

import com.sustech.so_java_stats.dto.TopicTrendResponseDto;
import com.sustech.so_java_stats.model.Question;
import com.sustech.so_java_stats.model.Tag;
import com.sustech.so_java_stats.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final QuestionRepository questionRepository;

    @Transactional(readOnly = true)
    public TopicTrendResponseDto getTopicTrends(List<String> requestedTopics,
                                                LocalDate startDate,
                                                LocalDate endDate,
                                                String granularity) {

        // 1. Convert LocalDate to Instant for DB Query (UTC)
        // Start of the first day
        Instant startInstant = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
        // End of the last day (start of next day)
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();

        List<Question> questions = questionRepository.findByCreationDateBetween(startInstant, endInstant);

        // 2. Prepare Date Formatter based on granularity
        DateTimeFormatter formatter;
        if ("yearly".equalsIgnoreCase(granularity)) {
            formatter = DateTimeFormatter.ofPattern("yyyy");
        } else {
            // Default to monthly
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        }

        // 3. Initialize data structure: Map<Topic, Map<DateBucket, Count>>
        Map<String, Map<String, Integer>> tempAggregator = new HashMap<>();
        for (String topic : requestedTopics) {
            tempAggregator.put(topic, new TreeMap<>()); // TreeMap to keep dates sorted
        }

        // 4. Iterate questions and bucket data
        for (Question q : questions) {
            String dateBucket = q.getCreationDate()
                    .atZone(ZoneId.of("UTC"))
                    .format(formatter);

            for (Tag tag : q.getTags()) {
                String tagName = tag.getTagName();
                // Only process if this tag was requested
                if (tempAggregator.containsKey(tagName)) {
                    tempAggregator.get(tagName)
                            .merge(dateBucket, 1, Integer::sum);
                }
            }
        }

        // 5. Convert to DTO format
        Map<String, List<TopicTrendResponseDto.TimePoint>> finalData = new HashMap<>();

        for (String topic : requestedTopics) {
            List<TopicTrendResponseDto.TimePoint> timePoints = tempAggregator.get(topic).entrySet().stream()
                    .map(entry -> new TopicTrendResponseDto.TimePoint(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            finalData.put(topic, timePoints);
        }

        return new TopicTrendResponseDto(requestedTopics, finalData);
    }
}