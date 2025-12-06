package com.sustech.so_java_stats.dto;

import java.util.List;
import java.util.Map;

public record QuestionSolvabilityResponseDto(
        Map<String, Integer> groupCounts,
        List<FactorComparison> factors
) {
    public record FactorComparison(
            String factorName,
            double solvableValue,
            double hardToSolveValue,
            String unit
    ) {
    }
}
