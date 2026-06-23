package com.lms.service;

import com.lms.dto.SubmissionResponse;
import com.lms.entity.Assignment;
import com.lms.entity.Course;
import com.lms.entity.Student;
import com.lms.entity.Submission;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.StudentRepository;
import com.lms.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private com.lms.repository.EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseProgressService progressService;

    @InjectMocks
    private SubmissionService submissionService;

    private Student student;
    private Assignment assignment;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(10L);

        com.lms.entity.Instructor instructor = new com.lms.entity.Instructor();
        instructor.setId(100L);

        course = new Course();
        course.setId(1L);
        course.setInstructor(instructor);

        assignment = new Assignment();
        assignment.setId(100L);
        assignment.setCourse(course);
        assignment.setMaxScore(100.0);
        assignment.setDueDate(LocalDateTime.now().plusDays(5));
    }

    @Test
    void submitAssignment_Success_OnTime() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(assignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(true);
        when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArguments()[0]);

        SubmissionResponse response = submissionService.submitAssignment(10L, 100L, "Homework Content");

        assertNotNull(response);
        assertEquals("Homework Content", response.getContent());
        verify(submissionRepository).save(any(Submission.class));
    }

    @Test
    void submitAssignment_NotEnrolled_ThrowsException() {
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(assignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> submissionService.submitAssignment(10L, 100L, "Content"));
    }

    @Test
    void submitAssignment_TooLate_ThrowsException() {
        // Due date was 4 days ago
        assignment.setDueDate(LocalDateTime.now().minusDays(4));

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(assignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> submissionService.submitAssignment(10L, 100L, "Content"));
    }

    @Test
    void gradeAssignment_OnTime_FullScore() {
        Submission submission = new Submission();
        submission.setId(500L);
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setSubmittedAt(LocalDateTime.now().minusDays(1)); // On time

        when(submissionRepository.findById(500L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArguments()[0]);

        SubmissionResponse response = submissionService.gradeAssignment(100L, 500L, 100.0);

        assertEquals(100.0, response.getScore());
    }

    @Test
    void gradeAssignment_Late_AppliesPenalty() {
        assignment.setDueDate(LocalDateTime.now().minusDays(2)); // Due 2 days ago

        Submission submission = new Submission();
        submission.setId(500L);
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setSubmittedAt(LocalDateTime.now()); // Submitted now (2 days late)

        when(submissionRepository.findById(500L)).thenReturn(Optional.of(submission));
        when(submissionRepository.save(any(Submission.class))).thenAnswer(i -> i.getArguments()[0]);

        // Base score 100. Late by 2 days = 20% penalty
        SubmissionResponse response = submissionService.gradeAssignment(100L, 500L, 100.0);

        assertEquals(80.0, response.getScore());
    }

    @Test
    void gradeAssignment_UnauthorizedInstructor_ThrowsException() {
        Submission submission = new Submission();
        submission.setId(500L);
        submission.setAssignment(assignment);
        submission.setStudent(student);

        when(submissionRepository.findById(500L)).thenReturn(Optional.of(submission));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> submissionService.gradeAssignment(999L, 500L, 90.0));
    }

    @Test
    void gradeAssignment_ExceedsMaxScore_ThrowsException() {
        Submission submission = new Submission();
        submission.setId(500L);
        submission.setAssignment(assignment);
        submission.setStudent(student);

        when(submissionRepository.findById(500L)).thenReturn(Optional.of(submission));

        assertThrows(BadRequestException.class, () -> submissionService.gradeAssignment(100L, 500L, 150.0));
    }
}
