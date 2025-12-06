package com.sustech.so_java_stats.controller;

import com.sustech.so_java_stats.dto.QuestionDetailResponseDto;
import com.sustech.so_java_stats.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
@Tag(name = "QuestionController", description = "Controller for question-related operations.")
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "Get Question Details", description = "Fetches full details for a specific question, including answers and comments.")
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponseDto> getQuestionDetails(
            @Parameter(description = "The StackOverflow Question ID", example = "106591")
            @PathVariable("id") Long questionId
    ) {
        try {
            QuestionDetailResponseDto response = questionService.getQuestionDetails(questionId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
