package com.sustech.so_java_stats.service.impl;

import com.sustech.so_java_stats.dto.QuestionDetailResponseDto;
import com.sustech.so_java_stats.model.Comment;
import com.sustech.so_java_stats.model.Question;
import com.sustech.so_java_stats.model.StackUser;
import com.sustech.so_java_stats.model.Tag;
import com.sustech.so_java_stats.repository.QuestionRepository;
import com.sustech.so_java_stats.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    @Override
    @Transactional(readOnly = true)
    public QuestionDetailResponseDto getQuestionDetails(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + questionId));

        QuestionDetailResponseDto.StackUserDto ownerDto = mapUser(question.getOwner());

        List<String> tagNames = question.getTags().stream()
                .map(Tag::getTagName)
                .collect(Collectors.toList());

        List<QuestionDetailResponseDto.CommentDto> commentDtos = mapComments(question.getComments());

        List<QuestionDetailResponseDto.AnswerDto> answerDtos = question.getAnswers().stream()
                .map(answer -> new QuestionDetailResponseDto.AnswerDto(
                        answer.getAnswerId(),
                        answer.getBody(),
                        answer.getCreationDate(),
                        answer.getScore(),
                        answer.getIsAccepted(),
                        mapUser(answer.getOwner()),
                        mapComments(answer.getComments())
                ))
                .collect(Collectors.toList());

        return new QuestionDetailResponseDto(
                question.getQuestionId(),
                question.getTitle(),
                question.getBody(),
                question.getCreationDate(),
                question.getScore(),
                question.getViewCount(),
                question.getAnswerCount(),
                question.getIsAnswered(),
                tagNames,
                ownerDto,
                commentDtos,
                answerDtos
        );
    }

    private QuestionDetailResponseDto.StackUserDto mapUser(StackUser stackUser) {
        if (stackUser == null) return null;
        return new QuestionDetailResponseDto.StackUserDto(
                stackUser.getStackUserId(),
                stackUser.getDisplayName(),
                stackUser.getReputation()
        );
    }

    private List<QuestionDetailResponseDto.CommentDto> mapComments(List<Comment> comments) {
        if (comments == null) return Collections.emptyList();
        return comments.stream()
                .map(comment -> new QuestionDetailResponseDto.CommentDto(
                        comment.getCommentId(),
                        comment.getBody(),
                        comment.getCreationDate(),
                        comment.getScore(),
                        comment.getOwner() != null ? comment.getOwner().getDisplayName() : "Unknown"
                ))
                .collect(Collectors.toList());
    }
}
