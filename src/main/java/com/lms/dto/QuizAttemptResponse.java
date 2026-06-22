package com.lms.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuizAttemptResponse {
    private Long id;
    private Long quizId;
    private Long studentId;
    private Double score;
    private String status;
    private Integer attemptNumber;
    private LocalDateTime submittedAt;
    private java.util.List<AnswerDto> studentAnswers;
}
