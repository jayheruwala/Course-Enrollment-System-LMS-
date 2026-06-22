package com.lms.service;

import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.EnrollmentStatus;
import com.lms.entity.Student;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.lms.dto.EnrollmentResponse;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public EnrollmentResponse enrollStudent(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        java.util.Optional<Enrollment> existingOpt = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existingOpt.isPresent()) {
            if (existingOpt.get().getStatus() != EnrollmentStatus.DROPPED) {
                throw new BadRequestException("Cannot enroll in same course twice");
            }
        }

        int activeEnrollments = enrollmentRepository.countByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE);
        if (activeEnrollments >= 5) {
            throw new BadRequestException("Limit reached: Student can enroll in maximum 5 courses simultaneously");
        }

        List<Enrollment> courseEnrollments = enrollmentRepository.findByCourseId(courseId);
        long currentStudents = courseEnrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE || e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();

        if (currentStudents >= course.getMaxStudents()) {
            throw new BadRequestException("Course full: Maximum capacity reached");
        }

        Enrollment enrollment;
        if (existingOpt.isPresent()) {
            enrollment = existingOpt.get();
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setEnrollmentDate(java.time.LocalDate.now());
            enrollment.setProgress(0.0);
        } else {
            enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollment.setEnrollmentDate(java.time.LocalDate.now());
            enrollment.setStatus(EnrollmentStatus.ACTIVE);
            enrollment.setProgress(0.0);
        }

        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentResponse dropCourse(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
        
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        return mapToResponse(enrollmentRepository.save(enrollment));
    }

    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
    }

    public Double getCourseProgress(Long userId, boolean isAdmin, boolean isStudent, Long studentId, Long courseId) {
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found for this student and course"));

        if (isStudent && !userId.equals(studentId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access Denied: You can only view your own progress");
        }

        if (!isStudent && !isAdmin) {
            if (!enrollment.getCourse().getInstructor().getId().equals(userId)) {
                throw new org.springframework.security.access.AccessDeniedException("Access Denied: Instructor does not own this course");
            }
        }

        return enrollment.getProgress();
    }

    private EnrollmentResponse mapToResponse(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setStudentId(enrollment.getStudent().getId());
        response.setCourseId(enrollment.getCourse().getId());
        response.setEnrollmentDate(enrollment.getEnrollmentDate());
        response.setStatus(enrollment.getStatus());
        response.setProgress(enrollment.getProgress());
        response.setCompletionDate(enrollment.getCompletionDate());
        return response;
    }
}
