package com.sustech.so_java_stats.service;

import com.sustech.so_java_stats.dto.MultithreadingPitfallResponseDto;
import com.sustech.so_java_stats.dto.QuestionSolvabilityResponseDto;
import com.sustech.so_java_stats.dto.TopicCooccurrenceResponseDto;
import com.sustech.so_java_stats.dto.TopicTrendResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface StatsService {

    TopicTrendResponseDto getTopicTrends(
            List<String> requestedTopics,
            LocalDate startDate,
            LocalDate endDate,
            String granularity
    );

    List<TopicCooccurrenceResponseDto> getTopicCooccurrences(
            int topN,
            int minFrequency,
            List<String> excludedTags
    );

    List<MultithreadingPitfallResponseDto> getMultithreadingPitfalls(
            int topN
    );

    QuestionSolvabilityResponseDto getQuestionSolvabilityFactors();
}
