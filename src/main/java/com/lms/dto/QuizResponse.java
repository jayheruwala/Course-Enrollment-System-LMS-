package com.lms.dto;

import lombok.Data;

@Data
public class QuizResponse {
    private Long id;
    private Long courseId;
    private String title;
    private java.util.List<QuestionDto> questions;
    private Double passingScore;
    private Integer timeLimit;
}
