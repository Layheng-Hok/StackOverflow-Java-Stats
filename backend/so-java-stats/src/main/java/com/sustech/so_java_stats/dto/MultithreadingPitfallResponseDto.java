package com.sustech.so_java_stats.dto;

import java.util.List;

public record MultithreadingPitfallResponseDto(
        String pitfall,
        int count,
        List<Long> examples
) {
}
