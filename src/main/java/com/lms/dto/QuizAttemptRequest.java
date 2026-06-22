package com.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuizAttemptRequest {

    @NotNull(message = "Answers array is required")
    private java.util.List<AnswerDto> answers;

    @NotNull(message = "Duration taken is required (in minutes)")
    private Integer durationTakenMinutes;
}
