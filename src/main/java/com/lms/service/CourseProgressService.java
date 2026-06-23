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
    private final AssignmentRepository assignmentRepository;
    private final QuizRepository quizRepository;
    private final SubmissionRepository submissionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public Enrollment updateProgress(Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        int totalLessons = lessonRepository.countByCourseId(courseId);
        int totalAssignments = assignmentRepository.countByCourseId(courseId);
        int totalQuizzes = quizRepository.findByCourseId(courseId).size();

        long completedLessonsCount = student.getCompletedLessons().stream()
                .filter(l -> l.getCourse().getId().equals(courseId))
                .count();

        int submittedAssignmentsCount = submissionRepository.countByStudentIdAndAssignmentCourseId(studentId, courseId);
        int passedQuizzesCount = quizAttemptRepository.countDistinctQuizzesPassedByStudentInCourse(studentId, courseId);

        // Rule 5: Course progress = completed lessons / total lessons
        double progress = 0.0;
        if (totalLessons > 0) {
            progress = ((double) completedLessonsCount / totalLessons) * 100;
        }
        enrollment.setProgress(progress);
        
        // Rule 8: Course completion requires: all lessons + all assignments + final quiz >= passingScore
        boolean allLessonsCompleted = (totalLessons == 0) || (completedLessonsCount >= totalLessons);
        boolean allAssignmentsSubmitted = (totalAssignments == 0) || (submittedAssignmentsCount >= totalAssignments);
        boolean allQuizzesPassed = (totalQuizzes == 0) || (passedQuizzesCount >= totalQuizzes);

        if (allLessonsCompleted && allAssignmentsSubmitted && allQuizzesPassed) {
            checkAndCompleteCourse(enrollment);
        }

        return enrollmentRepository.save(enrollment);
    }

    private void checkAndCompleteCourse(Enrollment enrollment) {
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
