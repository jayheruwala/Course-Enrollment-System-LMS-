package com.lms.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDto {
    private String questionText;
    private List<String> options;
    private String correctAnswer;
}
