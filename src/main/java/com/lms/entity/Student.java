package com.lms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Table(name = "students")
@Getter
@Setter
@SQLDelete(sql = "UPDATE students SET is_deleted = true WHERE id=?")
@Where(clause = "is_deleted=false")
public class Student extends User {

    @Column(nullable = false)
    private LocalDate enrollmentDate;

    @Column(nullable = false)
    private String status; // e.g., ACTIVE, INACTIVE

    @jakarta.persistence.ManyToMany
    @jakarta.persistence.JoinTable(
        name = "student_completed_lessons",
        joinColumns = @jakarta.persistence.JoinColumn(name = "student_id"),
        inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "lesson_id")
    )
    private java.util.Set<Lesson> completedLessons = new java.util.HashSet<>();
}
