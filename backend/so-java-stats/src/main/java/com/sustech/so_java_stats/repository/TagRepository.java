package com.sustech.so_java_stats.repository;

import com.sustech.so_java_stats.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
