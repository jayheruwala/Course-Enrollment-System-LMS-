package com.lms.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubmissionResponse {
    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String content;
    private LocalDateTime submittedAt;
    private Double score;
    private String feedback;
}
