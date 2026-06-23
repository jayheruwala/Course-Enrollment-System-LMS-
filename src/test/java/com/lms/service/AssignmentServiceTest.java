package com.lms.service;

import com.lms.dto.AssignmentRequest;
import com.lms.dto.AssignmentResponse;
import com.lms.dto.SubmissionResponse;
import com.lms.entity.Assignment;
import com.lms.entity.Course;
import com.lms.entity.Instructor;
import com.lms.entity.Student;
import com.lms.entity.Submission;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssignmentServiceTest {

    @Mock
    private AssignmentRepository assignmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private AssignmentService assignmentService;

    private Instructor instructor;
    private Course course;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        instructor = new Instructor();
        instructor.setId(1L);

        course = new Course();
        course.setId(10L);
        course.setInstructor(instructor);

        assignment = new Assignment();
        assignment.setId(100L);
        assignment.setCourse(course);
        assignment.setTitle("Test Assignment");
        assignment.setMaxScore(100.0);
    }

    @Test
    void addAssignment_Success() {
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("Test Assignment");
        request.setMaxScore(100.0);

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> i.getArguments()[0]);

        AssignmentResponse response = assignmentService.addAssignment(1L, 10L, request);

        assertNotNull(response);
        assertEquals("Test Assignment", response.getTitle());
        assertEquals(10L, response.getCourseId());
        verify(assignmentRepository).save(any(Assignment.class));
    }

    @Test
    void addAssignment_NotInstructor_ThrowsException() {
        AssignmentRequest request = new AssignmentRequest();

        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThrows(AccessDeniedException.class, () -> assignmentService.addAssignment(2L, 10L, request));
    }

    @Test
    void getAssignmentSubmissions_Success() {
        Student student = new Student();
        student.setId(5L);

        Submission submission = new Submission();
        submission.setId(500L);
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setContent("My Homework");
        submission.setSubmittedAt(LocalDateTime.now());

        when(assignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));
        when(submissionRepository.findByAssignmentId(100L)).thenReturn(List.of(submission));

        List<SubmissionResponse> responses = assignmentService.getAssignmentSubmissions(1L, 100L);

        assertEquals(1, responses.size());
        assertEquals(500L, responses.get(0).getId());
        assertEquals(5L, responses.get(0).getStudentId());
        assertEquals("My Homework", responses.get(0).getContent());
    }

    @Test
    void getAssignmentSubmissions_NotInstructor_ThrowsException() {
        when(assignmentRepository.findById(100L)).thenReturn(Optional.of(assignment));

        assertThrows(AccessDeniedException.class, () -> assignmentService.getAssignmentSubmissions(2L, 100L));
    }
}
