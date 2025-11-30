package com.sustech.so_java_stats.repository;

import com.sustech.so_java_stats.model.StackUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StackUserRepository extends JpaRepository<StackUser, Long> {
}
