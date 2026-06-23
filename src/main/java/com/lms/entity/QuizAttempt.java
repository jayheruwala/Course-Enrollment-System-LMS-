package com.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Getter
@Setter
@SQLDelete(sql = "UPDATE quiz_attempts SET is_deleted = true WHERE id=?")
@Where(clause = "is_deleted=false")
public class QuizAttempt extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private Double score;

    @Column(nullable = false)
    private String status; // PASSED or FAILED

    @Column(nullable = false)
    private Integer attemptNumber;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "student_answers")
    private java.util.List<com.lms.dto.AnswerDto> studentAnswers;
}
