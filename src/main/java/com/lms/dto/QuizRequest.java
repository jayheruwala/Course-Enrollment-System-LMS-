package com.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class QuizRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Questions array is required")
    private java.util.List<QuestionDto> questions;

    @NotNull(message = "Passing score is required")
    @Positive(message = "Passing score must be positive")
    private Double passingScore;

    @NotNull(message = "Time limit is required")
    @Positive(message = "Time limit must be positive")
    private Integer timeLimit;
}
