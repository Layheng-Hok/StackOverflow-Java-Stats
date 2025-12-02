package com.sustech.so_java_stats.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "answers")
@Data
public class Answer {
    @Id
    private Long answerId;

    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(columnDefinition = "TEXT")
    private String body;

    private Instant creationDate;

    private Instant lastEditDate;

    private Instant lastActivityDate;

    private Integer score;

    private Boolean isAccepted;

    @ManyToOne(cascade = CascadeType.ALL)
    private StackUser owner;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();
}
