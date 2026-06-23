package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.AssignmentRequest;
import com.lms.dto.AssignmentResponse;
import com.lms.dto.SubmissionResponse;
import com.lms.security.UserDetailsImpl;
import com.lms.service.AssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AssignmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AssignmentService assignmentService;

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
    void addAssignment_Success() throws Exception {
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("Test Assignment");
        request.setDescription("Test Description");
        request.setDueDate(LocalDateTime.now().plusDays(5));
        request.setMaxScore(100.0);

        AssignmentResponse response = new AssignmentResponse();
        response.setId(10L);
        response.setTitle("Test Assignment");

        when(assignmentService.addAssignment(eq(1L), eq(5L), any(AssignmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/courses/5/assignments")
                .with(user(instructorUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void addAssignment_AsStudent_Forbidden() throws Exception {
        AssignmentRequest request = new AssignmentRequest();
        request.setTitle("Test Assignment");
        request.setDescription("Test Description");
        request.setDueDate(LocalDateTime.now().plusDays(5));
        request.setMaxScore(100.0);

        mockMvc.perform(post("/api/courses/5/assignments")
                .with(user(studentUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAssignmentSubmissions_Success() throws Exception {
        SubmissionResponse response = new SubmissionResponse();
        response.setId(100L);
        response.setContent("Submission Content");

        when(assignmentService.getAssignmentSubmissions(1L, 10L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/assignments/10/submissions")
                .with(user(instructorUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(100L));
    }

    @Test
    void getAssignmentSubmissions_AsStudent_Forbidden() throws Exception {
        mockMvc.perform(get("/api/assignments/10/submissions")
                .with(user(studentUserDetails)))
                .andExpect(status().isForbidden());
    }
}
