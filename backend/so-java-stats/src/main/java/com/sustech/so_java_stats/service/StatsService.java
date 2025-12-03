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
import java.util.*;
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

        Instant startInstant = startDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.of("UTC")).toInstant();
        List<Question> questions = questionRepository.findByCreationDateBetween(startInstant, endInstant);

        DateTimeFormatter formatter;
        boolean isYearly = granularity.equalsIgnoreCase("yearly");
        if (isYearly) {
            formatter = DateTimeFormatter.ofPattern("yyyy");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        }

        List<String> allDateBuckets = new ArrayList<>();
        LocalDate current = startDate.withDayOfMonth(1);
        while (!current.isAfter(endDate)) {
            allDateBuckets.add(current.format(formatter));

            if (isYearly) {
                current = current.plusYears(1);
            } else {
                current = current.plusMonths(1);
            }
        }

        Map<String, Map<String, Integer>> tempAggregator = new HashMap<>();
        for (String topic : requestedTopics) {
            Map<String, Integer> timeSeries = new TreeMap<>();
            for (String bucket : allDateBuckets) {
                timeSeries.put(bucket, 0);
            }
            tempAggregator.put(topic, timeSeries);
        }

        for (Question question : questions) {
            String dateBucket = question.getCreationDate()
                    .atZone(ZoneId.of("UTC"))
                    .format(formatter);

            for (Tag tag : question.getTags()) {
                String tagName = tag.getTagName();
                if (tempAggregator.containsKey(tagName)) {
                    if (tempAggregator.get(tagName).containsKey(dateBucket)) {
                        tempAggregator.get(tagName).merge(dateBucket, 1, Integer::sum);
                    }
                }
            }
        }

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
