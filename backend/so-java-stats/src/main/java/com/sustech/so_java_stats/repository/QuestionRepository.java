package com.sustech.so_java_stats.repository;

import com.sustech.so_java_stats.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
