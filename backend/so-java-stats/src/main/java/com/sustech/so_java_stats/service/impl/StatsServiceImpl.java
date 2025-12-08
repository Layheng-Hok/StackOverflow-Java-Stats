package com.sustech.so_java_stats.service.impl;

import com.sustech.so_java_stats.dto.MultithreadingPitfallResponseDto;
import com.sustech.so_java_stats.dto.QuestionSolvabilityResponseDto;
import com.sustech.so_java_stats.dto.TopicCooccurrenceResponseDto;
import com.sustech.so_java_stats.dto.TopicTrendResponseDto;
import com.sustech.so_java_stats.model.Answer;
import com.sustech.so_java_stats.model.Comment;
import com.sustech.so_java_stats.model.Question;
import com.sustech.so_java_stats.repository.QuestionRepository;
import com.sustech.so_java_stats.repository.projection.TopicCooccurrenceProjection;
import com.sustech.so_java_stats.repository.projection.TopicTrendProjection;
import com.sustech.so_java_stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
        PITFALL_PATTERNS.put("Thread Leak", Pattern.compile("\\b(thread leak|thread exhaustion)\\b", Pattern.CASE_INSENSITIVE));
        PITFALL_PATTERNS.put("IllegalMonitorStateException", Pattern.compile("IllegalMonitorStateException"));
    }

    @Override
    @Transactional(readOnly = true)
    public TopicTrendResponseDto getTopicTrends(List<String> topics, LocalDate startDate, LocalDate endDate, String granularity) {

        Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toInstant();

        List<TopicTrendProjection> projections;

        switch (granularity.toLowerCase()) {
            case "year", "yearly" ->
                    projections = questionRepository.findTopicTrendsYearly(topics, startInstant, endInstant);
            case "month", "monthly" ->
                    projections = questionRepository.findTopicTrendsMonthly(topics, startInstant, endInstant);
            default -> throw new IllegalArgumentException("Invalid granularity: " + granularity);
        }

        Map<String, List<TopicTrendResponseDto.TimePoint>> mappedData = new HashMap<>();
        topics.forEach(t -> mappedData.put(t, new ArrayList<>()));

        for (TopicTrendProjection projection : projections) {
            String topic = projection.getTopic();
            if (mappedData.containsKey(topic)) {
                mappedData.get(topic).add(new TopicTrendResponseDto.TimePoint(
                        projection.getDatePoint(),
                        projection.getCountVal().intValue()
                ));
            }
        }

        return new TopicTrendResponseDto(topics, mappedData);
    }

    @Override
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

    @Override
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

            if (question.getComments() != null) {
                for (Comment comment : question.getComments()) {
                    contentBuilder.append(comment.getBody()).append(" ");
                }
            }

            if (question.getAnswers() != null) {
                for (Answer answer : question.getAnswers()) {
                    contentBuilder.append(answer.getBody()).append(" ");

                    if (answer.getComments() != null) {
                        for (Comment comment : answer.getComments()) {
                            contentBuilder.append(comment.getBody()).append(" ");
                        }
                    }
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
                .filter(responseDto -> responseDto.count() > 0)
                .sorted(Comparator.comparingInt(MultithreadingPitfallResponseDto::count).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionSolvabilityResponseDto getQuestionSolvabilityFactors() {
        List<Question> questions = questionRepository.findAll();

        List<Question> solvable = questions.stream()
                .filter(question -> question.getAcceptedAnswerId() != null)
                .toList();

        List<Question> hard = questions.stream()
                .filter(question -> question.getAcceptedAnswerId() == null)
                .toList();

        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Solvable", solvable.size());
        counts.put("Hard-to-Solve", hard.size());

        List<QuestionSolvabilityResponseDto.FactorComparison> factors = new ArrayList<>();

        factors.add(new QuestionSolvabilityResponseDto.FactorComparison(
                "Average Owner Reputation",
                calculateAverageReputation(solvable),
                calculateAverageReputation(hard),
                "score"
        ));

        factors.add(new QuestionSolvabilityResponseDto.FactorComparison(
                "Average Body Length",
                calculateAverageBodyLength(solvable),
                calculateAverageBodyLength(hard),
                "chars"
        ));

        factors.add(new QuestionSolvabilityResponseDto.FactorComparison(
                "Average Question Score",
                calculateAverageScore(solvable),
                calculateAverageScore(hard),
                "votes"
        ));

        return new QuestionSolvabilityResponseDto(counts, factors);
    }

    private double calculateAverageReputation(List<Question> questions) {
        return questions.stream()
                .map(Question::getOwner)
                .filter(Objects::nonNull)
                .mapToInt(user -> user.getReputation() != null ? user.getReputation() : 0)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageBodyLength(List<Question> questions) {
        return questions.stream()
                .mapToInt(question -> question.getBody() != null ? question.getBody().length() : 0)
                .average()
                .orElse(0.0);
    }

    private double calculateAverageScore(List<Question> questions) {
        return questions.stream()
                .mapToInt(question -> question.getScore() != null ? question.getScore() : 0)
                .average()
                .orElse(0.0);
    }
}
