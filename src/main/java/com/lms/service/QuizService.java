package com.lms.service;

import com.lms.dto.QuizAttemptRequest;
import com.lms.dto.QuizRequest;
import com.lms.entity.Course;
import com.lms.entity.Quiz;
import com.lms.entity.QuizAttempt;
import com.lms.entity.Student;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.dto.QuizResponse;
import com.lms.dto.QuizAttemptResponse;
import com.lms.repository.CourseRepository;
import com.lms.repository.QuizAttemptRepository;
import com.lms.repository.QuizRepository;
import com.lms.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final com.lms.repository.EnrollmentRepository enrollmentRepository;

    @Transactional
    public QuizResponse createQuiz(Long courseId, QuizRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setTitle(request.getTitle());
        quiz.setQuestions(request.getQuestions());
        quiz.setPassingScore(request.getPassingScore());
        quiz.setTimeLimit(request.getTimeLimit());

        return mapToQuizResponse(quizRepository.save(quiz));
    }

    @Transactional
    public QuizAttemptResponse submitQuizAttempt(Long studentId, Long quizId, QuizAttemptRequest request) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, quiz.getCourse().getId())) {
            throw new BadRequestException("Student is not enrolled in this course");
        }

        if (request.getDurationTakenMinutes() > quiz.getTimeLimit()) {
            throw new BadRequestException("Time exceeded: You took " + request.getDurationTakenMinutes() 
                    + " mins, limit is " + quiz.getTimeLimit() + " mins");
        }

        List<QuizAttempt> previousAttempts = quizAttemptRepository.findByStudentIdAndQuizId(studentId, quizId);
        if (previousAttempts.size() >= 3) { 
            throw new BadRequestException("Max retakes reached");
        }

        double score = evaluateAnswers(quiz.getQuestions(), request.getAnswers());
        String status = score >= quiz.getPassingScore() ? "PASSED" : "FAILED";

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudent(student);
        attempt.setStudentAnswers(request.getAnswers());
        attempt.setScore(score);
        attempt.setStatus(status);
        attempt.setAttemptNumber(previousAttempts.size() + 1);
        attempt.setSubmittedAt(LocalDateTime.now());

        return mapToQuizAttemptResponse(quizAttemptRepository.save(attempt));
    }

    private double evaluateAnswers(java.util.List<com.lms.dto.QuestionDto> questions, java.util.List<com.lms.dto.AnswerDto> answers) {
        if (questions == null || questions.isEmpty()) return 0.0;
        if (answers == null || answers.isEmpty()) return 0.0;

        int correctCount = 0;
        for (com.lms.dto.QuestionDto q : questions) {
            for (com.lms.dto.AnswerDto a : answers) {
                if (q.getQuestionText() != null && q.getQuestionText().equals(a.getQuestionText())) {
                    if (q.getCorrectAnswer() != null && q.getCorrectAnswer().equalsIgnoreCase(a.getSelectedAnswer())) {
                        correctCount++;
                    }
                    break;
                }
            }
        }
        return ((double) correctCount / questions.size()) * 100.0;
    }

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        QuizResponse res = new QuizResponse();
        res.setId(quiz.getId());
        res.setCourseId(quiz.getCourse().getId());
        res.setTitle(quiz.getTitle());
        res.setQuestions(quiz.getQuestions());
        res.setPassingScore(quiz.getPassingScore());
        res.setTimeLimit(quiz.getTimeLimit());
        return res;
    }

    private QuizAttemptResponse mapToQuizAttemptResponse(QuizAttempt attempt) {
        QuizAttemptResponse res = new QuizAttemptResponse();
        res.setId(attempt.getId());
        res.setQuizId(attempt.getQuiz().getId());
        res.setStudentId(attempt.getStudent().getId());
        res.setScore(attempt.getScore());
        res.setStatus(attempt.getStatus());
        res.setAttemptNumber(attempt.getAttemptNumber());
        res.setSubmittedAt(attempt.getSubmittedAt());
        res.setStudentAnswers(attempt.getStudentAnswers());
        return res;
    }
}
