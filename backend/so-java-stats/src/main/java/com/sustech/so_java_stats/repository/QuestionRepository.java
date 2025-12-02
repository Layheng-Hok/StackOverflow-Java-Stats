package com.sustech.so_java_stats.repository;

import com.sustech.so_java_stats.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = """
            SELECT t.tag_name, date_trunc(:granularity, q.creation_date) AS bucket, COUNT(*) AS cnt
            FROM questions q
            JOIN question_tags qt ON q.question_id = qt.question_id
            JOIN tags t ON qt.tag_name = t.tag_name
            WHERE t.tag_name IN (:topics)
              AND q.creation_date >= :startDate
              AND q.creation_date < :endDate
            GROUP BY t.tag_name, bucket
            ORDER BY t.tag_name, bucket
            """, nativeQuery = true)
    List<Object[]> findTrends(@Param("topics") List<String> topics,
                              @Param("startDate") Instant startDate,
                              @Param("endDate") Instant endDate,
                              @Param("granularity") String granularity);
}
