package com.lms.controller;

import com.lms.dto.ApiResponse;
import com.lms.dto.AssignmentRequest;
import com.lms.dto.AssignmentResponse;
import com.lms.dto.SubmissionResponse;
import com.lms.security.UserDetailsImpl;
import com.lms.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    @PostMapping("/courses/{courseId}/assignments")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> addAssignment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long courseId,
            @Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse assignment = assignmentService.addAssignment(userDetails.getId(), courseId, request);
        return new ResponseEntity<>(ApiResponse.success("Assignment created successfully", assignment), HttpStatus.CREATED);
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getAssignmentSubmissions(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(ApiResponse.success("Submissions retrieved successfully", assignmentService.getAssignmentSubmissions(userDetails.getId(), assignmentId)));
    }
}
