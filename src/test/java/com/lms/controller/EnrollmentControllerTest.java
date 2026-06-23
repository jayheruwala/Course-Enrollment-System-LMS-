package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.EnrollmentResponse;
import com.lms.security.UserDetailsImpl;
import com.lms.service.EnrollmentService;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnrollmentService enrollmentService;

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
    void enrollStudent_Success() throws Exception {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(10L);
        response.setCourseId(5L);

        when(enrollmentService.enrollStudent(1L, 5L)).thenReturn(response);

        mockMvc.perform(post("/api/enrollments/course/5")
                .with(user(studentUserDetails)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void enrollStudent_AsInstructor_Forbidden() throws Exception {
        mockMvc.perform(post("/api/enrollments/course/5")
                .with(user(instructorUserDetails)))
                .andExpect(status().isForbidden());
    }

    @Test
    void dropCourse_Success() throws Exception {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(10L);
        response.setStatus(com.lms.entity.EnrollmentStatus.DROPPED);

        when(enrollmentService.dropCourse(10L)).thenReturn(response);

        mockMvc.perform(put("/api/enrollments/10/drop")
                .with(user(studentUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DROPPED"));
    }

    @Test
    void getStudentEnrollments_Success() throws Exception {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(10L);

        when(enrollmentService.getStudentEnrollments(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/enrollments/my-enrollments")
                .with(user(studentUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10L));
    }

    @Test
    void generateCertificate_Success() throws Exception {
        mockMvc.perform(post("/api/enrollments/10/certificate")
                .with(user(studentUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getCourseProgress_Success() throws Exception {
        when(enrollmentService.getCourseProgress(1L, false, true, 1L, 5L)).thenReturn(75.5);

        mockMvc.perform(get("/api/enrollments/course/5/student/1/progress")
                .with(user(studentUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.progress").value(75.5));
    }
}
