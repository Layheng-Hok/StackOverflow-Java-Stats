package com.sustech.so_java_stats.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MultithreadingPitfallResponseDto(
        String pitfall,
        int count,
        @JsonProperty("question_ids") List<Long> questionIds
) {
}
