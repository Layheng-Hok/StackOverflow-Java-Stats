package com.sustech.so_java_stats.repository;

import com.sustech.so_java_stats.model.Question;
import com.sustech.so_java_stats.repository.projection.TopicCooccurrenceProjection;
import com.sustech.so_java_stats.repository.projection.TopicTrendProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = """
             SELECT qt.tag_name AS topic, TO_CHAR(q.creation_date, 'YYYY-MM') AS datePoint, COUNT(q.question_id) AS countVal
             FROM questions q
             JOIN question_tags qt ON q.question_id = qt.question_id
             WHERE qt.tag_name IN (:topics)
               AND q.creation_date BETWEEN :start AND :end
             GROUP BY qt.tag_name, TO_CHAR(q.creation_date, 'YYYY-MM')
             ORDER BY datePoint ASC
            """, nativeQuery = true)
    List<TopicTrendProjection> findTopicTrendsMonthly(
            @Param("topics") List<String> topics,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query(value = """
             SELECT qt.tag_name AS topic, TO_CHAR(q.creation_date, 'YYYY') AS datePoint, COUNT(q.question_id) AS countVal
             FROM questions q
             JOIN question_tags qt ON q.question_id = qt.question_id
             WHERE qt.tag_name IN (:topics)
               AND q.creation_date BETWEEN :start AND :end
             GROUP BY qt.tag_name, TO_CHAR(q.creation_date, 'YYYY')
             ORDER BY datePoint ASC
            """, nativeQuery = true)
    List<TopicTrendProjection> findTopicTrendsYearly(
            @Param("topics") List<String> topics,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    @Query(value = """
            SELECT t1.tag_name AS tag1, t2.tag_name AS tag2, COUNT(t1.question_id) AS frequency
            FROM question_tags t1
            JOIN question_tags t2 ON t1.question_id = t2.question_id
            WHERE t1.tag_name < t2.tag_name
              AND t1.tag_name != 'java'
              AND t2.tag_name != 'java'
              AND t1.tag_name NOT IN :excludedTags
              AND t2.tag_name NOT IN :excludedTags
            GROUP BY t1.tag_name, t2.tag_name
            HAVING COUNT(t1.question_id) >= :minFrequency
            ORDER BY frequency DESC
            LIMIT :topN
            """, nativeQuery = true)
    List<TopicCooccurrenceProjection> findTopicCooccurrencesWithExclude(
            @Param("topN") int topN,
            @Param("minFrequency") int minFrequency,
            @Param("excludedTags") List<String> excludedTags);

    @Query(value = """
            SELECT t1.tag_name AS tag1, t2.tag_name AS tag2, COUNT(t1.question_id) AS frequency
            FROM question_tags t1
            JOIN question_tags t2 ON t1.question_id = t2.question_id
            WHERE t1.tag_name < t2.tag_name
              AND t1.tag_name != 'java'
              AND t2.tag_name != 'java'
            GROUP BY t1.tag_name, t2.tag_name
            HAVING COUNT(t1.question_id) >= :minFrequency
            ORDER BY frequency DESC
            LIMIT :topN
            """, nativeQuery = true)
    List<TopicCooccurrenceProjection> findTopicCooccurrencesWithoutExclude(
            @Param("topN") int topN,
            @Param("minFrequency") int minFrequency);

    List<Question> findDistinctByTags_TagNameIn(List<String> tags);
}
