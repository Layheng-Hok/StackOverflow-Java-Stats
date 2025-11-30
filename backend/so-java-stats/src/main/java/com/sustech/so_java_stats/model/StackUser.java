package com.sustech.so_java_stats.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class StackUser {
    @Id
    private Long userId;

    private Integer reputation;

    private String displayName;
}
