package com.lms.service;

import com.lms.dto.CourseRequest;
import com.lms.dto.CourseResponse;
import com.lms.entity.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.InstructorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseService courseService;

    private Instructor instructor;
    private Course course;
    private CourseRequest request;

    @BeforeEach
    void setUp() {
        instructor = new Instructor();
        instructor.setId(1L);

        course = new Course();
        course.setId(10L);
        course.setInstructor(instructor);
        course.setTitle("Java Basics");

        request = new CourseRequest();
        request.setTitle("Advanced Java");
    }

    @Test
    void createCourse_Success() {
        when(instructorRepository.findById(1L)).thenReturn(Optional.of(instructor));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArguments()[0]);

        CourseResponse response = courseService.createCourse(1L, request);

        assertNotNull(response);
        assertEquals("Advanced Java", response.getTitle());
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void updateCourse_Success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArguments()[0]);

        CourseResponse response = courseService.updateCourse(1L, 10L, request);

        assertEquals("Advanced Java", response.getTitle());
    }

    @Test
    void updateCourse_NotOwner_ThrowsAccessDenied() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        assertThrows(AccessDeniedException.class, () -> courseService.updateCourse(2L, 10L, request));
    }

    @Test
    void deleteCourse_Success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(1L, 10L);

        verify(courseRepository).delete(course);
    }

    @Test
    void searchCourses_Success() {
        when(courseRepository.findAll()).thenReturn(List.of(course));

        List<CourseResponse> results = courseService.searchCourses(null, "Java");

        assertEquals(1, results.size());
    }

    @Test
    void getCourseStudents_Success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        
        Student student = new Student();
        student.setId(100L);
        student.setName("John");
        student.setRole(Role.ROLE_STUDENT);
        
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);

        when(enrollmentRepository.findByCourseId(10L)).thenReturn(List.of(enrollment));

        var students = courseService.getCourseStudents(1L, false, 10L);
        assertEquals(1, students.size());
        assertEquals("John", students.get(0).getName());
    }

    @Test
    void getCourseCompletionRate_Success() {
        when(courseRepository.findById(10L)).thenReturn(Optional.of(course));
        
        Enrollment e1 = new Enrollment();
        e1.setStatus(EnrollmentStatus.COMPLETED);
        
        Enrollment e2 = new Enrollment();
        e2.setStatus(EnrollmentStatus.ACTIVE);

        when(enrollmentRepository.findByCourseId(10L)).thenReturn(List.of(e1, e2));

        Double rate = courseService.getCourseCompletionRate(1L, false, 10L);

        assertEquals(50.0, rate);
    }
}
