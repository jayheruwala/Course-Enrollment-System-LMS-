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

    @Mock
    private com.lms.repository.CertificateRepository certificateRepository;

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

    @Test
    void getCourseProgress_Success() {
        com.lms.entity.Instructor instructor = new com.lms.entity.Instructor();
        instructor.setId(5L);
        course.setInstructor(instructor);

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);
        enrollment.setProgress(75.0);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.of(enrollment));

        // Test as Student (self)
        Double progress = enrollmentService.getCourseProgress(1L, false, true, 1L, 1L);
        assertEquals(75.0, progress);

        // Test as Admin
        progress = enrollmentService.getCourseProgress(99L, true, false, 1L, 1L);
        assertEquals(75.0, progress);

        // Test as Instructor (owner)
        progress = enrollmentService.getCourseProgress(5L, false, false, 1L, 1L);
        assertEquals(75.0, progress);
    }

    @Test
    void getCourseProgress_AccessDenied() {
        com.lms.entity.Instructor instructor = new com.lms.entity.Instructor();
        instructor.setId(5L);
        course.setInstructor(instructor);

        Enrollment enrollment = new Enrollment();
        enrollment.setCourse(course);

        when(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(Optional.of(enrollment));

        // Test Student trying to view another student's progress
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> 
            enrollmentService.getCourseProgress(2L, false, true, 1L, 1L));

        // Test Instructor trying to view a course they don't own
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> 
            enrollmentService.getCourseProgress(9L, false, false, 1L, 1L));
    }

    @Test
    void generateCertificate_Success() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(100L);
        enrollment.setStudent(student);
        course.setTitle("Java Basics");
        enrollment.setCourse(course);
        enrollment.setStatus(EnrollmentStatus.COMPLETED);

        student.setName("Alice");

        com.lms.entity.Certificate certificate = new com.lms.entity.Certificate();
        certificate.setIssueDate(java.time.LocalDate.now());
        certificate.setUrl("https://lms.com/certificates/100");

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));
        when(certificateRepository.findByEnrollmentId(100L)).thenReturn(Optional.of(certificate));

        java.util.Map<String, String> res = enrollmentService.generateCertificate(1L, 100L);

        assertNotNull(res);
        assertEquals("Alice", res.get("studentName"));
        assertEquals("Java Basics", res.get("courseTitle"));
        assertEquals("https://lms.com/certificates/100", res.get("certificateUrl"));
    }

    @Test
    void generateCertificate_NotCompleted_ThrowsException() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(100L);
        enrollment.setStudent(student);
        enrollment.setStatus(EnrollmentStatus.ACTIVE);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));

        assertThrows(BadRequestException.class, () -> enrollmentService.generateCertificate(1L, 100L));
    }

    @Test
    void generateCertificate_WrongStudent_ThrowsException() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(100L);
        enrollment.setStudent(student);

        when(enrollmentRepository.findById(100L)).thenReturn(Optional.of(enrollment));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> enrollmentService.generateCertificate(2L, 100L));
    }
}
