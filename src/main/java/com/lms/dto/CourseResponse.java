package com.lms.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private Long instructorId;
    private Integer duration;
    private BigDecimal price;
    private Integer maxStudents;
    private com.lms.entity.CourseCategory category;
}
