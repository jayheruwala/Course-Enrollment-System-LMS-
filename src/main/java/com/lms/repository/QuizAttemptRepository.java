package com.lms.repository;

import com.lms.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByStudentIdAndQuizId(Long studentId, Long quizId);
    int countByStudentIdAndQuizId(Long studentId, Long quizId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT qa.quiz.id) FROM QuizAttempt qa WHERE qa.student.id = :studentId AND qa.quiz.course.id = :courseId")
    int countDistinctQuizzesAttemptedByStudentInCourse(Long studentId, Long courseId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT qa.quiz.id) FROM QuizAttempt qa WHERE qa.student.id = :studentId AND qa.quiz.course.id = :courseId AND qa.status = 'PASSED'")
    int countDistinctQuizzesPassedByStudentInCourse(Long studentId, Long courseId);
}
