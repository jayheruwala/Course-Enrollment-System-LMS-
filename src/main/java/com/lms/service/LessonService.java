package com.lms.service;

import com.lms.dto.LessonRequest;
import com.lms.entity.Course;
import com.lms.entity.Lesson;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import com.lms.dto.LessonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final CourseProgressService progressService;
    private final com.lms.repository.StudentRepository studentRepository;
    private final com.lms.repository.EnrollmentRepository enrollmentRepository;

    @Transactional
    public LessonResponse addLesson(Long instructorId, Long courseId, LessonRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not have permission to add lessons to this course");
        }

        int currentLessonCount = lessonRepository.countByCourseId(courseId);

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(request.getTitle());
        lesson.setContent(request.getContent());
        lesson.setDuration(request.getDuration());
        lesson.setOrderIndex(currentLessonCount + 1);

        return mapToResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public void markLessonComplete(Long studentId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        
        com.lms.entity.Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
                
        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, lesson.getCourse().getId())) {
            throw new com.lms.exception.BadRequestException("Student is not enrolled in this course");
        }
        
        student.getCompletedLessons().add(lesson);
        studentRepository.save(student);
        
        progressService.updateProgress(studentId, lesson.getCourse().getId());
    }

    private LessonResponse mapToResponse(Lesson lesson) {
        LessonResponse res = new LessonResponse();
        res.setId(lesson.getId());
        res.setCourseId(lesson.getCourse().getId());
        res.setTitle(lesson.getTitle());
        res.setContent(lesson.getContent());
        res.setOrderIndex(lesson.getOrderIndex());
        res.setDuration(lesson.getDuration());
        return res;
    }
}
