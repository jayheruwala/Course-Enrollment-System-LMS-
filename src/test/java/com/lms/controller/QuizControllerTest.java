package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.QuizAttemptRequest;
import com.lms.dto.QuizAttemptResponse;
import com.lms.dto.QuizRequest;
import com.lms.dto.QuizResponse;
import com.lms.security.UserDetailsImpl;
import com.lms.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuizService quizService;

    private UserDetailsImpl instructorUserDetails;
    private UserDetailsImpl studentUserDetails;

    @BeforeEach
    void setUp() {
        instructorUserDetails = new UserDetailsImpl(
                1L, "instructor@test.com", "Instructor", "password", List.of(new SimpleGrantedAuthority("ROLE_INSTRUCTOR"))
        );

        studentUserDetails = new UserDetailsImpl(
                2L, "student@test.com", "Student", "password", List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    @Test
    void createQuiz_Success() throws Exception {
        QuizRequest request = new QuizRequest();
        request.setTitle("Math Quiz");
        request.setPassingScore(50.0);
        request.setTimeLimit(30);
        request.setQuestions(List.of());

        QuizResponse response = new QuizResponse();
        response.setId(10L);
        response.setTitle("Math Quiz");

        when(quizService.createQuiz(eq(5L), any(QuizRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/courses/5/quizzes")
                .with(user(instructorUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void createQuiz_AsStudent_Forbidden() throws Exception {
        QuizRequest request = new QuizRequest();
        request.setTitle("Math Quiz");
        request.setPassingScore(50.0);
        request.setTimeLimit(30);
        request.setQuestions(List.of());

        mockMvc.perform(post("/api/courses/5/quizzes")
                .with(user(studentUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitQuizAttempt_Success() throws Exception {
        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setDurationTakenMinutes(15);
        request.setAnswers(List.of());

        QuizAttemptResponse response = new QuizAttemptResponse();
        response.setId(100L);
        response.setScore(90.0);
        response.setStatus("PASSED");

        when(quizService.submitQuizAttempt(eq(2L), eq(10L), any(QuizAttemptRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/quizzes/10/attempt")
                .with(user(studentUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(90.0))
                .andExpect(jsonPath("$.data.status").value("PASSED"));
    }

    @Test
    void submitQuizAttempt_AsInstructor_Forbidden() throws Exception {
        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setDurationTakenMinutes(15);
        request.setAnswers(List.of());

        mockMvc.perform(post("/api/quizzes/10/attempt")
                .with(user(instructorUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
