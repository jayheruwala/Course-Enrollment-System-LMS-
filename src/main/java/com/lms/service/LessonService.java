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

    @Transactional
    public LessonResponse addLesson(Long courseId, LessonRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

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
    public void markLessonComplete(Long studentId, Long courseId, int completedLessonsCount) {
        progressService.updateProgress(studentId, courseId, completedLessonsCount);
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
