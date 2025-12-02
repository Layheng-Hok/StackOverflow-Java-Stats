package com.sustech.so_java_stats.service;

import com.sustech.so_java_stats.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final QuestionRepository questionRepository;

    public Map<String, Object> getTopicTrends(List<String> topics, LocalDate startDate, LocalDate endDate, String granularity) {
        // Compute time range in Instant (UTC)
        ZoneOffset utc = ZoneOffset.UTC;
        Instant startInst = startDate.atStartOfDay(utc).toInstant();
        Instant endInst = endDate.plusDays(1).atStartOfDay(utc).toInstant();

        // Determine DB granularity string for date_trunc
        String dbGran = "monthly".equals(granularity) ? "month" : "year";

        // Query raw counts
        List<Object[]> rawResults = questionRepository.findTrends(topics, startInst, endInst, dbGran);

        // Map: tag -> bucket -> count
        Map<String, Map<String, Long>> tagToBucketCount = new HashMap<>();
        for (Object[] row : rawResults) {
            String tag = (String) row[0];
            Instant ts = (Instant) row[1];
            long cnt = ((Number) row[2]).longValue();
            String bucket = formatBucket(ts, granularity);
            tagToBucketCount.computeIfAbsent(tag, k -> new HashMap<>()).put(bucket, cnt);
        }

        // Generate all expected buckets
        List<String> allBuckets = generateBuckets(startDate, endDate, granularity);

        // Build series per topic (fill missing with 0)
        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        for (String topic : topics) {
            Map<String, Long> bucketCount = tagToBucketCount.getOrDefault(topic, Collections.emptyMap());
            List<Map<String, Object>> series = allBuckets.stream()
                    .map(bucket -> Map.<String, Object>of(
                            "date", bucket,
                            "value", bucketCount.getOrDefault(bucket, 0L)
                    ))
                    .toList();
            data.put(topic, series);
        }

        return Map.of(
                "topics", topics,
                "data", data
        );
    }

    private List<String> generateBuckets(LocalDate start, LocalDate end, String granularity) {
        if ("monthly".equals(granularity)) {
            YearMonth startYm = YearMonth.from(start);
            YearMonth endYm = YearMonth.from(end);
            List<String> buckets = new ArrayList<>();
            YearMonth current = startYm;
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
            while (!current.isAfter(endYm)) {
                buckets.add(current.format(fmt));
                current = current.plusMonths(1);

            }
            return buckets;
        } else {
            int startY = start.getYear();
            int endY = end.getYear();
            return IntStream.rangeClosed(startY, endY)
                    .mapToObj(String::valueOf)
                    .toList();
        }
    }

    private String formatBucket(Instant ts, String granularity) {
        LocalDateTime ldt = LocalDateTime.ofInstant(ts, ZoneOffset.UTC);
        if ("monthly".equals(granularity)) {
            return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        } else {
            return String.valueOf(ldt.getYear());
        }
    }
}