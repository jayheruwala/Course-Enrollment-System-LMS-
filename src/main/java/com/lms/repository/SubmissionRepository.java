package com.lms.repository;

import com.lms.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    List<Submission> findByAssignmentId(Long assignmentId);
    List<Submission> findByStudentId(Long studentId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(s) FROM Submission s WHERE s.student.id = :studentId AND s.assignment.course.id = :courseId")
    int countByStudentIdAndAssignmentCourseId(Long studentId, Long courseId);
}
