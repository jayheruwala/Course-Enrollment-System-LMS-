package com.lms.service;

import com.lms.entity.Certificate;
import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.EnrollmentStatus;
import com.lms.entity.Lesson;
import com.lms.entity.Student;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CertificateRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.LessonRepository;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.QuizRepository;
import com.lms.repository.SubmissionRepository;
import com.lms.repository.QuizAttemptRepository;
import com.lms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseProgressServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private CourseProgressService progressService;

    private Enrollment enrollment;
    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(10L);
        student.setCompletedLessons(new java.util.HashSet<>());

        Course course = new Course();
        course.setId(1L);

        enrollment = new Enrollment();
        enrollment.setId(100L);
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setProgress(0.0);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);
    }

    @Test
    void updateProgress_PartialCompletion_UpdatesProgress() {
        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 1L)).thenReturn(Optional.of(enrollment));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(lessonRepository.countByCourseId(1L)).thenReturn(10);
        when(assignmentRepository.countByCourseId(1L)).thenReturn(0);
        when(quizRepository.findByCourseId(1L)).thenReturn(java.util.Collections.emptyList());
        when(submissionRepository.countByStudentIdAndAssignmentCourseId(10L, 1L)).thenReturn(0);
        when(quizAttemptRepository.countDistinctQuizzesPassedByStudentInCourse(10L, 1L)).thenReturn(0);
        
        // 5 lessons completed
        java.util.Set<Lesson> completedLessons = new java.util.HashSet<>();
        for (int i=0; i<5; i++) {
            Lesson l = new Lesson();
            l.setCourse(enrollment.getCourse());
            completedLessons.add(l);
        }
        student.setCompletedLessons(completedLessons);

        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArguments()[0]);

        Enrollment result = progressService.updateProgress(10L, 1L);

        assertEquals(50.0, result.getProgress());
        assertEquals(EnrollmentStatus.ACTIVE, result.getStatus());
        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    @Test
    void updateProgress_FullCompletion_MarksCompleteAndIssuesCertificate() {
        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 1L)).thenReturn(Optional.of(enrollment));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(lessonRepository.countByCourseId(1L)).thenReturn(10);
        when(assignmentRepository.countByCourseId(1L)).thenReturn(0);
        when(quizRepository.findByCourseId(1L)).thenReturn(java.util.Collections.emptyList());
        when(submissionRepository.countByStudentIdAndAssignmentCourseId(10L, 1L)).thenReturn(0);
        when(quizAttemptRepository.countDistinctQuizzesPassedByStudentInCourse(10L, 1L)).thenReturn(0);
        when(certificateRepository.findByEnrollmentId(100L)).thenReturn(Optional.empty());
        
        java.util.Set<Lesson> completedLessons = new java.util.HashSet<>();
        for (int i=0; i<10; i++) {
            Lesson l = new Lesson();
            l.setCourse(enrollment.getCourse());
            completedLessons.add(l);
        }
        student.setCompletedLessons(completedLessons);
        
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> i.getArguments()[0]);

        Enrollment result = progressService.updateProgress(10L, 1L);

        assertEquals(100.0, result.getProgress());
        assertEquals(EnrollmentStatus.COMPLETED, result.getStatus());
        assertNotNull(result.getCompletionDate());
        verify(certificateRepository, times(1)).save(any(Certificate.class));
    }

    @Test
    void updateProgress_FullCompletion_CertificateAlreadyExists() {
        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 1L)).thenReturn(Optional.of(enrollment));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(lessonRepository.countByCourseId(1L)).thenReturn(10);
        when(assignmentRepository.countByCourseId(1L)).thenReturn(0);
        when(quizRepository.findByCourseId(1L)).thenReturn(java.util.Collections.emptyList());
        when(submissionRepository.countByStudentIdAndAssignmentCourseId(10L, 1L)).thenReturn(0);
        when(quizAttemptRepository.countDistinctQuizzesPassedByStudentInCourse(10L, 1L)).thenReturn(0);
        
        java.util.Set<Lesson> completedLessons = new java.util.HashSet<>();
        for (int i=0; i<10; i++) {
            Lesson l = new Lesson();
            l.setCourse(enrollment.getCourse());
            completedLessons.add(l);
        }
        student.setCompletedLessons(completedLessons);
        
        // Mock that certificate already exists
        when(certificateRepository.findByEnrollmentId(100L)).thenReturn(Optional.of(new Certificate()));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(enrollment);

        Enrollment updated = progressService.updateProgress(10L, 1L);

        assertEquals(100.0, updated.getProgress());
        assertEquals(EnrollmentStatus.COMPLETED, updated.getStatus());
        
        // Should NOT save another certificate
        verify(certificateRepository, never()).save(any(Certificate.class));
    }

    @Test
    void updateProgress_EnrollmentNotFound_ThrowsException() {
        when(enrollmentRepository.findByStudentIdAndCourseId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> progressService.updateProgress(10L, 1L));
    }
}
