package com.lms.service;

import com.lms.entity.Assignment;
import com.lms.entity.Student;
import com.lms.entity.Submission;
import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.StudentRepository;
import com.lms.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lms.dto.SubmissionResponse;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final StudentRepository studentRepository;
    private final com.lms.repository.EnrollmentRepository enrollmentRepository;

    @Transactional
    public SubmissionResponse submitAssignment(Long studentId, Long assignmentId, String content) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));

        if (!enrollmentRepository.existsByStudentIdAndCourseId(studentId, assignment.getCourse().getId())) {
            throw new BadRequestException("Student is not enrolled in this course");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = assignment.getDueDate();

        if (now.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate.toLocalDate(), now.toLocalDate());
            if (now.toLocalTime().isAfter(dueDate.toLocalTime()) && daysLate == 0) {
                daysLate = 1; // It's late on the same day after due time
            }
            if (daysLate > 3) {
                throw new BadRequestException("Too late: Submission allowed max 3 days past due date");
            }
        }

        Submission submission = new Submission();
        submission.setStudent(student);
        submission.setAssignment(assignment);
        submission.setContent(content);
        submission.setSubmittedAt(now);

        return mapToResponse(submissionRepository.save(submission));
    }

    @Transactional
    public SubmissionResponse gradeAssignment(Long submissionId, Double score) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found"));

        LocalDateTime dueDate = submission.getAssignment().getDueDate();
        LocalDateTime submittedAt = submission.getSubmittedAt();

        Double finalScore = score;
        if (submittedAt.isAfter(dueDate)) {
            long daysLate = ChronoUnit.DAYS.between(dueDate.toLocalDate(), submittedAt.toLocalDate());
            if (submittedAt.toLocalTime().isAfter(dueDate.toLocalTime()) && daysLate == 0) {
                daysLate = 1;
            }
            // 10% penalty per day
            double penaltyPercentage = daysLate * 0.10;
            finalScore = score - (score * penaltyPercentage);
        }

        submission.setScore(finalScore);
        return mapToResponse(submissionRepository.save(submission));
    }

    private SubmissionResponse mapToResponse(Submission submission) {
        SubmissionResponse res = new SubmissionResponse();
        res.setId(submission.getId());
        res.setAssignmentId(submission.getAssignment().getId());
        res.setStudentId(submission.getStudent().getId());
        res.setContent(submission.getContent());
        res.setSubmittedAt(submission.getSubmittedAt());
        res.setScore(submission.getScore());
        res.setFeedback(submission.getFeedback());
        return res;
    }
}
