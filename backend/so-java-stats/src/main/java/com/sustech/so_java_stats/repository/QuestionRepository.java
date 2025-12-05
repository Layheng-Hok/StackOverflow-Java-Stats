package com.sustech.so_java_stats.repository;

import com.sustech.so_java_stats.model.Question;
import com.sustech.so_java_stats.repository.projection.TopicCooccurrenceProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCreationDateBetween(Instant startDate, Instant endDate);

    @Query(value = """
            SELECT t1.tag_name AS tag1, t2.tag_name AS tag2, COUNT(t1.question_id) AS frequency
            FROM question_tags t1
            JOIN question_tags t2 ON t1.question_id = t2.question_id
            WHERE t1.tag_name < t2.tag_name
              AND t1.tag_name != 'java'
              AND t2.tag_name != 'java'
              AND t1.tag_name NOT IN :excluded
              AND t2.tag_name NOT IN :excluded
            GROUP BY t1.tag_name, t2.tag_name
            HAVING COUNT(t1.question_id) >= :minFrequency
            ORDER BY frequency DESC
            LIMIT :topN
            """, nativeQuery = true)
    List<TopicCooccurrenceProjection> findTopicCooccurrencesWithExclude(@Param("topN") int topN,
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
    List<TopicCooccurrenceProjection> findTopicCooccurrencesWithoutExclude(@Param("topN") int topN,
                                                                           @Param("minFrequency") int minFrequency);

    List<Question> findDistinctByTags_TagNameIn(List<String> tags);
}
