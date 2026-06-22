package com.lms.controller;

import com.lms.dto.QuizAttemptRequest;
import com.lms.dto.QuizRequest;
import com.lms.entity.Quiz;
import com.lms.entity.QuizAttempt;
import com.lms.service.QuizService;
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
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/courses/{courseId}/quizzes")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<ApiResponse<com.lms.dto.QuizResponse>> createQuiz(
            @PathVariable Long courseId,
            @Valid @RequestBody QuizRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Quiz created successfully", quizService.createQuiz(courseId, request)), HttpStatus.CREATED);
    }

    @PostMapping("/quizzes/{quizId}/attempt")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<com.lms.dto.QuizAttemptResponse>> submitQuizAttempt(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long quizId,
            @Valid @RequestBody QuizAttemptRequest request) {
        return new ResponseEntity<>(ApiResponse.success("Quiz submitted successfully", quizService.submitQuizAttempt(userDetails.getId(), quizId, request)), HttpStatus.CREATED);
    }
}
