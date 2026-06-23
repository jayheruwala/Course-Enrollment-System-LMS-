package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.LessonRequest;
import com.lms.dto.LessonResponse;
import com.lms.security.UserDetailsImpl;
import com.lms.service.LessonService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LessonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService lessonService;

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
    void addLesson_Success() throws Exception {
        LessonRequest request = new LessonRequest();
        request.setTitle("Test Lesson");
        request.setContent("Test Content");
        request.setDuration(60);

        LessonResponse response = new LessonResponse();
        response.setId(10L);
        response.setTitle("Test Lesson");

        when(lessonService.addLesson(eq(2L), eq(5L), any(LessonRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/courses/5/lessons")
                .with(user(instructorUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void addLesson_AsStudent_Forbidden() throws Exception {
        LessonRequest request = new LessonRequest();
        request.setTitle("Test Lesson");
        request.setContent("Test Content");
        request.setDuration(60);

        mockMvc.perform(post("/api/courses/5/lessons")
                .with(user(studentUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void markLessonComplete_Success() throws Exception {
        mockMvc.perform(put("/api/lessons/1/complete")
                .with(user(studentUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(lessonService, times(1)).markLessonComplete(eq(1L), eq(1L));
    }

    @Test
    void markLessonComplete_AsInstructor_Forbidden() throws Exception {
        mockMvc.perform(put("/api/lessons/1/complete")
                .with(user(instructorUserDetails)))
                .andExpect(status().isForbidden());
    }
}
