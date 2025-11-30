package com.sustech.so_java_stats.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    private Long commentId;

    @Column(columnDefinition = "TEXT")
    private String body;

    private Instant creationDate;

    private Integer score;

    @ManyToOne(cascade = CascadeType.ALL)
    private StackUser owner;
}
