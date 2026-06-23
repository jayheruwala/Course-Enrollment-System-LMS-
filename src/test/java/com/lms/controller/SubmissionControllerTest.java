package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.SubmissionResponse;
import com.lms.security.UserDetailsImpl;
import com.lms.service.SubmissionService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubmissionService submissionService;

    private UserDetailsImpl studentUserDetails;
    private UserDetailsImpl instructorUserDetails;

    @BeforeEach
    void setUp() {
        studentUserDetails = new UserDetailsImpl(
                1L, "student@test.com", "Student", "password", List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        instructorUserDetails = new UserDetailsImpl(
                2L, "instructor@test.com", "Instructor", "password", List.of(new SimpleGrantedAuthority("ROLE_INSTRUCTOR"))
        );
    }

    @Test
    void submitAssignment_Success() throws Exception {
        Map<String, String> payload = Map.of("content", "My Submission");

        SubmissionResponse response = new SubmissionResponse();
        response.setId(10L);
        response.setContent("My Submission");

        when(submissionService.submitAssignment(eq(1L), eq(5L), eq("My Submission"))).thenReturn(response);

        mockMvc.perform(post("/api/submissions/assignment/5")
                .with(user(studentUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void submitAssignment_AsInstructor_Forbidden() throws Exception {
        Map<String, String> payload = Map.of("content", "My Submission");

        mockMvc.perform(post("/api/submissions/assignment/5")
                .with(user(instructorUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }

    @Test
    void gradeAssignment_Success() throws Exception {
        Map<String, Double> payload = Map.of("score", 95.0);

        SubmissionResponse response = new SubmissionResponse();
        response.setId(10L);
        response.setScore(95.0);

        when(submissionService.gradeAssignment(eq(2L), eq(10L), eq(95.0))).thenReturn(response);

        mockMvc.perform(put("/api/submissions/10/grade")
                .with(user(instructorUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.score").value(95.0));
    }

    @Test
    void gradeAssignment_AsStudent_Forbidden() throws Exception {
        Map<String, Double> payload = Map.of("score", 95.0);

        mockMvc.perform(put("/api/submissions/10/grade")
                .with(user(studentUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isForbidden());
    }
}
