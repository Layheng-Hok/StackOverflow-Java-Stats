package com.sustech.so_java_stats.dto;

import java.time.Instant;
import java.util.List;

public record QuestionDetailResponseDto(
        Long questionId,
        String title,
        String body,
        Instant creationDate,
        Integer score,
        Integer viewCount,
        Integer answerCount,
        Boolean isAnswered,
        List<String> tags,
        StackUserDto owner,
        List<CommentDto> comments,
        List<AnswerDto> answers
) {
    public record StackUserDto(
            Long stackUserId,
            String displayName,
            Integer reputation
    ) {
    }

    public record CommentDto(
            Long commentId,
            String body,
            Instant creationDate,
            Integer score,
            String ownerName
    ) {
    }

    public record AnswerDto(
            Long answerId,
            String body,
            Instant creationDate,
            Integer score,
            Boolean isAccepted,
            StackUserDto owner,
            List<CommentDto> comments
    ) {
    }
}
