package com.lms.controller;

import com.lms.entity.Enrollment;
import com.lms.service.EnrollmentService;
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
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.lms.dto.EnrollmentResponse>> enrollStudent(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long courseId) {
        com.lms.dto.EnrollmentResponse enrollment = enrollmentService.enrollStudent(userDetails.getId(), courseId);
        return new ResponseEntity<>(ApiResponse.success("Enrolled successfully", enrollment), HttpStatus.CREATED);
    }

    @PutMapping("/{enrollmentId}/drop")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.lms.dto.EnrollmentResponse>> dropCourse(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(ApiResponse.success("Course dropped successfully", enrollmentService.dropCourse(enrollmentId)));
    }

    @GetMapping("/my-enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<com.lms.dto.EnrollmentResponse>>> getStudentEnrollments(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Enrollments retrieved successfully", enrollmentService.getStudentEnrollments(userDetails.getId())));
    }

    @PostMapping("/{enrollmentId}/certificate")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateCertificate(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long enrollmentId) {
        return new ResponseEntity<>(ApiResponse.success("Certificate retrieved successfully", enrollmentService.generateCertificate(userDetails.getId(), enrollmentId)), HttpStatus.OK);
    }

    @GetMapping("/course/{courseId}/student/{studentId}/progress")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN') or hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Map<String, Double>>> getCourseProgress(
            @PathVariable Long courseId,
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStudent = userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        
        Double progress = enrollmentService.getCourseProgress(userDetails.getId(), isAdmin, isStudent, studentId, courseId);
        return ResponseEntity.ok(ApiResponse.success("Course progress retrieved successfully", Map.of("progress", progress)));
    }
}
