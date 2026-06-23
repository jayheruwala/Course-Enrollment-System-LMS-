package com.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.dto.CourseRequest;
import com.lms.dto.CourseResponse;
import com.lms.entity.CourseCategory;
import com.lms.entity.Role;
import com.lms.security.UserDetailsImpl;
import com.lms.service.CourseService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourseService courseService;

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
    void createCourse_Success() throws Exception {
        CourseRequest request = new CourseRequest();
        request.setTitle("Test Course");
        request.setDescription("Test Description");
        request.setPrice(java.math.BigDecimal.valueOf(99.99));
        request.setMaxStudents(50);
        request.setDuration(120);
        request.setCategory(CourseCategory.PROGRAMMING);

        CourseResponse response = new CourseResponse();
        response.setId(10L);
        response.setTitle("Test Course");

        when(courseService.createCourse(eq(1L), any(CourseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/courses")
                .with(user(instructorUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10L));
    }

    @Test
    void createCourse_AsStudent_Forbidden() throws Exception {
        CourseRequest request = new CourseRequest();
        request.setTitle("Test Course");
        request.setDescription("Test Description");
        request.setPrice(java.math.BigDecimal.valueOf(99.99));
        request.setMaxStudents(50);
        request.setDuration(120);
        request.setCategory(CourseCategory.PROGRAMMING);

        mockMvc.perform(post("/api/courses")
                .with(user(studentUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getInstructorCourses_Success() throws Exception {
        CourseResponse response = new CourseResponse();
        response.setId(10L);
        response.setTitle("Test Course");

        when(courseService.getInstructorCourses(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/courses/instructor")
                .with(user(instructorUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(10L));
    }

    @Test
    void searchCourses_Unauthenticated_Success() throws Exception {
        CourseResponse response = new CourseResponse();
        response.setId(10L);
        response.setTitle("Java Programming");

        when(courseService.searchCourses(null, "Java")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/courses/search?title=Java")
                .with(user(studentUserDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("Java Programming"));
    }

    @Test
    void deleteCourse_Success() throws Exception {
        mockMvc.perform(delete("/api/courses/10")
                .with(user(instructorUserDetails)))
                .andExpect(status().isOk());

        verify(courseService).deleteCourse(1L, 10L);
    }
}
