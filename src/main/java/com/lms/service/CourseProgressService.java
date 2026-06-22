package com.lms.service;

import com.lms.entity.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CourseProgressService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final CertificateRepository certificateRepository;

    @Transactional
    public Enrollment updateProgress(Long studentId, Long courseId, int completedLessonsCount) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        int totalLessons = lessonRepository.countByCourseId(courseId);
        double progress = 0.0;
        
        if (totalLessons > 0) {
            progress = ((double) completedLessonsCount / totalLessons) * 100;
        }

        enrollment.setProgress(progress);
        
        if (progress >= 100.0) {
            checkAndCompleteCourse(enrollment);
        }

        return enrollmentRepository.save(enrollment);
    }

    private void checkAndCompleteCourse(Enrollment enrollment) {
        // Logic to verify assignments and quizzes would go here in a real scenario
        // For now, if progress is 100%, mark as complete
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        enrollment.setCompletionDate(LocalDate.now());

        // Generate Certificate
        if (certificateRepository.findByEnrollmentId(enrollment.getId()).isEmpty()) {
            Certificate certificate = new Certificate();
            certificate.setEnrollment(enrollment);
            certificate.setIssueDate(LocalDate.now());
            certificate.setUrl("https://lms.com/certificates/" + enrollment.getId());
            certificateRepository.save(certificate);
        }
    }
}
