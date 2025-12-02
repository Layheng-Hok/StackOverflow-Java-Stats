package com.sustech.so_java_stats.dto;

import java.util.List;
import java.util.Map;

public record TopicTrendResponseDto(
        List<String> topics,
        Map<String, List<TimePoint>> data
) {

    public record TimePoint(
            String date,
            int value
    ) {
    }
}
