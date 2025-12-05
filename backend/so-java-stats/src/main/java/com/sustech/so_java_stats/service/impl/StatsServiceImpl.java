package com.sustech.so_java_stats.service.impl;

import com.sustech.so_java_stats.dto.MultithreadingPitfallResponseDto;
import com.sustech.so_java_stats.dto.TopicCooccurrenceResponseDto;
import com.sustech.so_java_stats.dto.TopicTrendResponseDto;
import com.sustech.so_java_stats.model.Answer;
import com.sustech.so_java_stats.model.Question;
import com.sustech.so_java_stats.model.Tag;
import com.sustech.so_java_stats.repository.QuestionRepository;
import com.sustech.so_java_stats.repository.projection.TopicCooccurrenceProjection;
import com.sustech.so_java_stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final QuestionRepository questionRepository;

    private static final List<String> CONCURRENCY_TAGS = List.of(
            "multithreading", "java-multithreading", "concurrency",
            "synchronization", "thread-safety", "parallel-processing",
            "executor-service", "fork-join", "atomic", "volatile", "locking"
    );

    private static final Map<String, Pattern> PITFALL_PATTERNS = new HashMap<>();

    static {
        PITFALL_PATTERNS.put("Deadlock", Pattern.compile("\\bdeadlock\\b", Pattern.CASE_INSENSITIVE));
        PITFALL_PATTERNS.put("Race Condition", Pattern.compile("\\brace condition\\b", Pattern.CASE_INSENSITIVE));
        PITFALL_PATTERNS.put("ConcurrentModificationException", Pattern.compile("ConcurrentModificationException"));
        PITFALL_PATTERNS.put("Memory Consistency / Visibility", Pattern.compile("\\b(visibility problem|memory consistency|volatile variable)\\b", Pattern.CASE_INSENSITIVE));
        PITFALL_PATTERNS.put("Thread Starvation", Pattern.compile("\\b(starvation|livelock)\\b", Pattern.CASE_INSENSITIVE));
        PITFALL_PATTERNS.put("IllegalMonitorStateException", Pattern.compile("IllegalMonitorStateException"));
    }

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

        Map<String, List<TopicTrendResponseDto.TimePoint>> finalData = new LinkedHashMap<>();
        for (String topic : requestedTopics) {
            List<TopicTrendResponseDto.TimePoint> timePoints = tempAggregator.get(topic).entrySet().stream()
                    .map(entry -> new TopicTrendResponseDto.TimePoint(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
            finalData.put(topic, timePoints);
        }

        return new TopicTrendResponseDto(requestedTopics, finalData);
    }

    @Transactional(readOnly = true)
    public List<TopicCooccurrenceResponseDto> getTopicCooccurrences(int topN,
                                                                    int minFrequency,
                                                                    List<String> excludedTags) {
        List<TopicCooccurrenceProjection> projections;
        if (excludedTags.isEmpty()) {
            projections = questionRepository.findTopicCooccurrencesWithoutExclude(topN, minFrequency);
        } else {
            projections = questionRepository.findTopicCooccurrencesWithExclude(topN, minFrequency, excludedTags);
        }
        return projections.stream()
                .map(projection -> new TopicCooccurrenceResponseDto(
                        List.of(projection.getTag1(), projection.getTag2()),
                        projection.getFrequency()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MultithreadingPitfallResponseDto> getMultithreadingPitfalls(int topN) {
        List<Question> relevantQuestions = questionRepository.findDistinctByTags_TagNameIn(CONCURRENCY_TAGS);

        Map<String, List<Long>> pitfallMatches = new HashMap<>();
        for (String key : PITFALL_PATTERNS.keySet()) {
            pitfallMatches.put(key, new ArrayList<>());
        }

        for (Question question : relevantQuestions) {
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append(question.getTitle()).append(" ");
            contentBuilder.append(question.getBody()).append(" ");
            if (question.getAnswers() != null) {
                for (Answer answer : question.getAnswers()) {
                    contentBuilder.append(answer.getBody()).append(" ");
                }
            }
            String fullContent = contentBuilder.toString();

            for (Map.Entry<String, Pattern> entry : PITFALL_PATTERNS.entrySet()) {
                Matcher matcher = entry.getValue().matcher(fullContent);
                if (matcher.find()) {
                    pitfallMatches.get(entry.getKey()).add(question.getQuestionId());
                }
            }
        }

        return pitfallMatches.entrySet().stream()
                .map(entry -> new MultithreadingPitfallResponseDto(
                        entry.getKey(),
                        entry.getValue().size(),
                        new ArrayList<>(entry.getValue())
                ))
                .filter(dto -> dto.count() > 0)
                .sorted(Comparator.comparingInt(MultithreadingPitfallResponseDto::count).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }
}
