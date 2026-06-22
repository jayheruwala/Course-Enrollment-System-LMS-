package com.lms.service;

import com.lms.dto.AssignmentRequest;
import com.lms.dto.AssignmentResponse;
import com.lms.dto.SubmissionResponse;
import com.lms.entity.Assignment;
import com.lms.entity.Course;
import com.lms.entity.Submission;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.CourseRepository;
import com.lms.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional
    public AssignmentResponse addAssignment(Long instructorId, Long courseId, AssignmentRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new AccessDeniedException("Only the course instructor can add assignments to this course");
        }

        Assignment assignment = new Assignment();
        assignment.setCourse(course);
        assignment.setTitle(request.getTitle());
        assignment.setDescription(request.getDescription());
        assignment.setDueDate(request.getDueDate());
        assignment.setMaxScore(request.getMaxScore());

        return mapToAssignmentResponse(assignmentRepository.save(assignment));
    }

    public List<SubmissionResponse> getAssignmentSubmissions(Long instructorId, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
                
        if (!assignment.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new AccessDeniedException("Only the course instructor can view submissions");
        }

        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::mapToSubmissionResponse)
                .collect(Collectors.toList());
    }

    private AssignmentResponse mapToAssignmentResponse(Assignment assignment) {
        AssignmentResponse res = new AssignmentResponse();
        res.setId(assignment.getId());
        res.setCourseId(assignment.getCourse().getId());
        res.setTitle(assignment.getTitle());
        res.setDescription(assignment.getDescription());
        res.setDueDate(assignment.getDueDate());
        res.setMaxScore(assignment.getMaxScore());
        return res;
    }

    private SubmissionResponse mapToSubmissionResponse(Submission submission) {
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
