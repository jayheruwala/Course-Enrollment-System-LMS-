package com.lms.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@SQLDelete(sql = "UPDATE lessons SET is_deleted = true WHERE id=?")
@Where(clause = "is_deleted=false")
public class Lesson extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, name = "order_index")
    private Integer orderIndex;

    @Column(nullable = false)
    private Integer duration; // in minutes
}
