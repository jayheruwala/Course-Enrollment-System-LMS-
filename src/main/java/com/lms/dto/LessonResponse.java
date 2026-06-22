package com.lms.dto;

import lombok.Data;

@Data
public class LessonResponse {
    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private Integer orderIndex;
    private Integer duration;
}
