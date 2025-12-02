package com.sustech.so_java_stats.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
@Data
public class Question {

    @Id
    private Long questionId;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    private Instant lastEditDate;

    private Instant lastActivityDate;

    private Instant creationDate;

    private Integer score;

    private Integer viewCount;

    private Integer answerCount;

    private Boolean isAnswered;

    private Long acceptedAnswerId;

    @ManyToOne(cascade = CascadeType.ALL)
    private StackUser owner;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_name")
    )
    private List<Tag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();
}
