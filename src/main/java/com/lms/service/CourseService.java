package com.lms.service;

import com.lms.dto.CourseRequest;
import com.lms.dto.CourseResponse;
import com.lms.entity.Course;
import com.lms.entity.Enrollment;
import com.lms.entity.EnrollmentStatus;
import com.lms.entity.Instructor;
import com.lms.entity.Student;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public CourseResponse createCourse(Long instructorId, CourseRequest request) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor not found"));

        Course course = new Course();
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setDuration(request.getDuration());
        course.setPrice(request.getPrice());
        course.setMaxStudents(request.getMaxStudents());
        course.setCategory(request.getCategory());
        course.setInstructor(instructor);

        Course savedCourse = courseRepository.save(course);
        return mapToResponse(savedCourse);
    }

    @Transactional
    public CourseResponse updateCourse(Long instructorId, Long courseId, CourseRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new org.springframework.security.access.AccessDeniedException("Instructor does not own this course (Forbidden)");
        }

        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setDuration(request.getDuration());
        course.setPrice(request.getPrice());
        course.setMaxStudents(request.getMaxStudents());
        course.setCategory(request.getCategory());

        Course updatedCourse = courseRepository.save(course);
        return mapToResponse(updatedCourse);
    }

    public List<CourseResponse> getInstructorCourses(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteCourse(Long instructorId, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new org.springframework.security.access.AccessDeniedException("Instructor does not own this course (Forbidden)");
        }
        
        courseRepository.delete(course);
    }

    public List<CourseResponse> searchCourses(com.lms.entity.CourseCategory category, String title) {
        return courseRepository.findAll().stream()
                .filter(c -> category == null || c.getCategory() == category)
                .filter(c -> title == null || c.getTitle().toLowerCase().contains(title.toLowerCase()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<com.lms.dto.UserResponse> getCourseStudents(Long userId, boolean isAdmin, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
                
        if (!isAdmin && !course.getInstructor().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Instructor does not own this course (Forbidden)");
        }

        return enrollmentRepository.findByCourseId(courseId)
                .stream().map(e -> {
                    com.lms.dto.UserResponse ur = new com.lms.dto.UserResponse();
                    ur.setId(e.getStudent().getId());
                    ur.setName(e.getStudent().getName());
                    ur.setEmail(e.getStudent().getEmail());
                    if (e.getStudent().getRole() != null) {
                        ur.setRole(e.getStudent().getRole().name());
                    }
                    return ur;
                }).collect(Collectors.toList());
    }

    public Double getCourseCompletionRate(Long userId, boolean isAdmin, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
                
        if (!isAdmin && !course.getInstructor().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Instructor does not own this course (Forbidden)");
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        if (enrollments.isEmpty()) return 0.0;

        long completedCount = enrollments.stream()
                .filter(e -> e.getStatus() == EnrollmentStatus.COMPLETED)
                .count();

        return (double) completedCount / enrollments.size() * 100;
    }

    private CourseResponse mapToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setId(course.getId());
        response.setTitle(course.getTitle());
        response.setDescription(course.getDescription());
        response.setInstructorId(course.getInstructor().getId());
        response.setDuration(course.getDuration());
        response.setPrice(course.getPrice());
        response.setMaxStudents(course.getMaxStudents());
        response.setCategory(course.getCategory());
        return response;
    }
}
