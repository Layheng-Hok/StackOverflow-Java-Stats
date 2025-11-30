package com.sustech.so_java_stats.repository;

import com.sustech.so_java_stats.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
