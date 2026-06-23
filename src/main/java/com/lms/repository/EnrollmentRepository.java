package com.lms.repository;

import com.lms.entity.Enrollment;
import com.lms.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudentId(Long studentId);
    List<Enrollment> findByCourseId(Long courseId);
    int countByStudentIdAndStatus(Long studentId, EnrollmentStatus status);
    List<Enrollment> findByStatusAndEnrollmentDateBefore(EnrollmentStatus status, java.time.LocalDate date);
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);
}
