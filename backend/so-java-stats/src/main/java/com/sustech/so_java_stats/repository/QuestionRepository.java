package com.sustech.so_java_stats.repository;

import com.sustech.so_java_stats.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByCreationDateBetween(Instant startDate, Instant endDate);
}