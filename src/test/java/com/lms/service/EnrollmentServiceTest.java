package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.EnrollmentStatus;
import com.lms.entity.Student;
import com.lms.exception.BadRequestException;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Student student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(1L);

        course = new Course();
        course.setId(1L);
        course.setMaxStudents(10);
    }

    @Test
    void enrollStudent_Success() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByStudentIdAndStatus(1L, EnrollmentStatus.ACTIVE)).thenReturn(2);
        when(enrollmentRepository.findByCourseId(1L)).thenReturn(Collections.emptyList());

        Enrollment expectedEnrollment = new Enrollment();
        expectedEnrollment.setId(1L);
        expectedEnrollment.setStudent(student);
        expectedEnrollment.setCourse(course);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(expectedEnrollment);

        com.lms.dto.EnrollmentResponse result = enrollmentService.enrollStudent(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_FailsIfAlreadyEnrolled() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        Enrollment existing = new Enrollment();
        existing.setStatus(EnrollmentStatus.ACTIVE);
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.of(existing));

        assertThrows(BadRequestException.class, () -> enrollmentService.enrollStudent(1L, 1L));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_FailsIfLimitReached() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByStudentIdAndStatus(1L, EnrollmentStatus.ACTIVE)).thenReturn(5);

        assertThrows(BadRequestException.class, () -> enrollmentService.enrollStudent(1L, 1L));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }

    @Test
    void enrollStudent_FailsIfCourseFull() {
        course.setMaxStudents(1);
        Enrollment activeEnrollment = new Enrollment();
        activeEnrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByStudentIdAndStatus(1L, EnrollmentStatus.ACTIVE)).thenReturn(2);
        when(enrollmentRepository.findByCourseId(1L)).thenReturn(Collections.singletonList(activeEnrollment));

        assertThrows(BadRequestException.class, () -> enrollmentService.enrollStudent(1L, 1L));
        verify(enrollmentRepository, never()).save(any(Enrollment.class));
    }
}
