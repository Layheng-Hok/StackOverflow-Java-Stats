package com.sustech.so_java_stats.service;

import com.sustech.so_java_stats.dto.QuestionDetailResponseDto;

public interface QuestionService {

    QuestionDetailResponseDto getQuestionDetails(Long questionId);
}
