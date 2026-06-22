package com.lms.controller;

import com.lms.dto.CourseRequest;
import com.lms.dto.CourseResponse;
import com.lms.entity.Student;
import com.lms.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.lms.security.UserDetailsImpl;
import com.lms.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.createCourse(userDetails.getId(), request);
        return new ResponseEntity<>(ApiResponse.success("Course created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseService.updateCourse(userDetails.getId(), courseId, request);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", response));
    }

    @GetMapping("/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getInstructorCourses(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Instructor courses retrieved successfully", courseService.getInstructorCourses(userDetails.getId())));
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        courseService.deleteCourse(userDetails.getId(), courseId);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> searchCourses(
            @RequestParam(required = false) com.lms.entity.CourseCategory category,
            @RequestParam(required = false) String title) {
        return ResponseEntity.ok(ApiResponse.success("Courses retrieved successfully", courseService.searchCourses(category, title)));
    }

    @GetMapping("/{courseId}/students")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<com.lms.dto.UserResponse>>> getCourseStudents(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long courseId) {
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(ApiResponse.success("Course students retrieved successfully", courseService.getCourseStudents(userDetails.getId(), isAdmin, courseId)));
    }

    @GetMapping("/{courseId}/completion-rate")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getCourseCompletionRate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long courseId) {
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Double rate = courseService.getCourseCompletionRate(userDetails.getId(), isAdmin, courseId);
        return ResponseEntity.ok(ApiResponse.success("Completion rate retrieved successfully", Map.of("completionRatePercentage", rate)));
    }
}
