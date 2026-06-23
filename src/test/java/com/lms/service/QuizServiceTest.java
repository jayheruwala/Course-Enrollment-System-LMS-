package com.lms.service;

import com.lms.dto.*;
import com.lms.entity.Course;
import com.lms.entity.Quiz;
import com.lms.entity.QuizAttempt;
import com.lms.entity.Student;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.CourseRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.QuizAttemptRepository;
import com.lms.repository.QuizRepository;
import com.lms.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private com.lms.repository.EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseProgressService progressService;

    @InjectMocks
    private QuizService quizService;

    private Course course;
    private Student student;
    private Quiz quiz;
    private QuestionDto questionDto;
    private AnswerDto answerDto;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);

        student = new Student();
        student.setId(10L);

        questionDto = new QuestionDto();
        questionDto.setQuestionText("What is 2+2?");
        questionDto.setCorrectAnswer("4");
        
        answerDto = new AnswerDto();
        answerDto.setQuestionText("What is 2+2?");
        answerDto.setSelectedAnswer("4");

        quiz = new Quiz();
        quiz.setId(100L);
        quiz.setCourse(course);
        quiz.setQuestions(List.of(questionDto));
        quiz.setPassingScore(50.0);
        quiz.setTimeLimit(30);
    }

    @Test
    void createQuiz_Success() {
        QuizRequest request = new QuizRequest();
        request.setTitle("Math Quiz");
        request.setPassingScore(50.0);
        request.setTimeLimit(30);
        request.setQuestions(List.of(questionDto));

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(i -> i.getArguments()[0]);

        QuizResponse response = quizService.createQuiz(1L, request);

        assertNotNull(response);
        assertEquals("Math Quiz", response.getTitle());
        assertEquals(50.0, response.getPassingScore());
        verify(quizRepository).save(any(Quiz.class));
    }

    @Test
    void submitQuizAttempt_Success_Passed() {
        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setDurationTakenMinutes(15);
        request.setAnswers(List.of(answerDto));

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(true);
        when(quizAttemptRepository.findByStudentIdAndQuizId(10L, 100L)).thenReturn(List.of());
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

        QuizAttemptResponse response = quizService.submitQuizAttempt(10L, 100L, request);

        assertEquals(100.0, response.getScore());
        assertEquals("PASSED", response.getStatus());
        assertEquals(1, response.getAttemptNumber());
    }

    @Test
    void submitQuizAttempt_Success_Failed() {
        answerDto.setSelectedAnswer("5"); // Wrong answer
        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setDurationTakenMinutes(15);
        request.setAnswers(List.of(answerDto));

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(true);
        when(quizAttemptRepository.findByStudentIdAndQuizId(10L, 100L)).thenReturn(List.of());
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArguments()[0]);

        QuizAttemptResponse response = quizService.submitQuizAttempt(10L, 100L, request);

        assertEquals(0.0, response.getScore());
        assertEquals("FAILED", response.getStatus());
    }

    @Test
    void submitQuizAttempt_TimeExceeded_ThrowsException() {
        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setDurationTakenMinutes(40); // Limit is 30

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> quizService.submitQuizAttempt(10L, 100L, request));
    }

    @Test
    void submitQuizAttempt_NotEnrolled_ThrowsException() {
        QuizAttemptRequest request = new QuizAttemptRequest();

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> quizService.submitQuizAttempt(10L, 100L, request));
    }

    @Test
    void submitQuizAttempt_MaxRetakesReached_ThrowsException() {
        QuizAttemptRequest request = new QuizAttemptRequest();
        request.setDurationTakenMinutes(10);

        when(studentRepository.findById(10L)).thenReturn(Optional.of(student));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(quiz));
        when(enrollmentRepository.existsByStudentIdAndCourseId(10L, 1L)).thenReturn(true);
        when(quizAttemptRepository.findByStudentIdAndQuizId(10L, 100L)).thenReturn(List.of(new QuizAttempt(), new QuizAttempt(), new QuizAttempt())); // 3 attempts already

        assertThrows(BadRequestException.class, () -> quizService.submitQuizAttempt(10L, 100L, request));
    }
}
