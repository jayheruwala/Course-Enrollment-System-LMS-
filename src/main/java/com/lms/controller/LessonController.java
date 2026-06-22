package com.lms.controller;

import com.lms.dto.LessonRequest;
import com.lms.entity.Lesson;
import com.lms.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.lms.security.UserDetailsImpl;
import com.lms.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/courses/{courseId}/lessons")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<com.lms.dto.LessonResponse>> addLesson(
            @PathVariable Long courseId,
            @Valid @RequestBody LessonRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Lesson created successfully", lessonService.addLesson(courseId, request)), HttpStatus.CREATED);
    }

    @PutMapping("/enrollments/courses/{courseId}/lessons/complete")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<Void>> markLessonComplete(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long courseId,
            @RequestParam int completedLessonsCount) {
        lessonService.markLessonComplete(userDetails.getId(), courseId, completedLessonsCount);
        return ResponseEntity.ok(ApiResponse.success("Lesson marked as complete", null));
    }
}
