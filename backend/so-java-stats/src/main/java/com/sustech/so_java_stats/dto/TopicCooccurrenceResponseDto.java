package com.sustech.so_java_stats.dto;

import java.util.List;

public record TopicCooccurrenceResponseDto(
        List<String> pair,
        Long frequency
) {
}