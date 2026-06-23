package com.lms.controller;

import com.lms.entity.Submission;
import com.lms.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.lms.security.UserDetailsImpl;
import com.lms.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<com.lms.dto.SubmissionResponse>> submitAssignment(@AuthenticationPrincipal UserDetailsImpl userDetails, 
                                              @PathVariable Long assignmentId, 
                                              @RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        return new ResponseEntity<>(ApiResponse.success("Assignment submitted successfully", submissionService.submitAssignment(userDetails.getId(), assignmentId, content)), HttpStatus.CREATED);
    }

    @PutMapping("/{submissionId}/grade")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<com.lms.dto.SubmissionResponse>> gradeAssignment(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long submissionId, 
            @RequestBody Map<String, Double> payload) {
        Double score = payload.get("score");
        return ResponseEntity.ok(ApiResponse.success("Assignment graded successfully", submissionService.gradeAssignment(userDetails.getId(), submissionId, score)));
    }
}
