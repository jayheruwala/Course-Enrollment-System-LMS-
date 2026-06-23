package com.lms.service;

import com.lms.dto.LessonRequest;
import com.lms.dto.LessonResponse;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseProgressService progressService;

    @Mock
    private com.lms.repository.StudentRepository studentRepository;

    @Mock
    private com.lms.repository.EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private LessonService lessonService;

    private Course course;
    private LessonRequest lessonRequest;

    @BeforeEach
    void setUp() {
        com.lms.entity.Instructor instructor = new com.lms.entity.Instructor();
        instructor.setId(100L);
        
        course = new Course();
        course.setId(1L);
        course.setInstructor(instructor);

        lessonRequest = new LessonRequest();
        lessonRequest.setTitle("Test Lesson");
        lessonRequest.setContent("Test Content");
        lessonRequest.setDuration(60);
    }

    @Test
    void addLesson_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(lessonRepository.countByCourseId(1L)).thenReturn(2);

        Lesson savedLesson = new Lesson();
        savedLesson.setId(10L);
        savedLesson.setCourse(course);
        savedLesson.setTitle(lessonRequest.getTitle());
        savedLesson.setContent(lessonRequest.getContent());
        savedLesson.setDuration(lessonRequest.getDuration());
        savedLesson.setOrderIndex(3);

        when(lessonRepository.save(any(Lesson.class))).thenReturn(savedLesson);

        LessonResponse response = lessonService.addLesson(100L, 1L, lessonRequest);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Test Lesson", response.getTitle());
        assertEquals(3, response.getOrderIndex());

        ArgumentCaptor<Lesson> lessonCaptor = ArgumentCaptor.forClass(Lesson.class);
        verify(lessonRepository).save(lessonCaptor.capture());

        Lesson capturedLesson = lessonCaptor.getValue();
        assertEquals(course, capturedLesson.getCourse());
        assertEquals("Test Lesson", capturedLesson.getTitle());
        assertEquals(3, capturedLesson.getOrderIndex());
    }

    @Test
    void addLesson_CourseNotFound_ThrowsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> lessonService.addLesson(100L, 1L, lessonRequest));
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void addLesson_UnauthorizedInstructor_ThrowsException() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> lessonService.addLesson(999L, 1L, lessonRequest));
        verify(lessonRepository, never()).save(any(Lesson.class));
    }

    @Test
    void markLessonComplete_Success() {
        com.lms.entity.Student student = new com.lms.entity.Student();
        student.setId(10L);
        student.setCompletedLessons(new java.util.HashSet<>());
        
        Course course = new Course();
        course.setId(1L);
        
        Lesson lesson = new Lesson();
        lesson.setId(1L);
        lesson.setCourse(course);

        when(lessonRepository.findById(1L)).thenReturn(Optional.of(lesson));
        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(true);

        lessonService.markLessonComplete(10L, 1L);

        verify(progressService, times(1)).updateProgress(10L, 1L);
        verify(studentRepository, times(1)).save(student);
    }
}
