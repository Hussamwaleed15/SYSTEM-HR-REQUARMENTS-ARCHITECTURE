package com.services.application.service;

import com.services.application.client.CandidateServiceClient;
import com.services.application.client.JobServiceClient;
import com.services.application.client.NotificationClient;
import com.services.application.dto.ApplicationResponse;
import com.services.application.dto.ApplicationStatusResponse;
import com.services.application.dto.AiMatchingResult;
import com.services.application.dto.AiValidationResult;
import com.services.application.model.Application;
import com.services.application.repository.ApplicationRepository;
import com.services.application.enums.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusMachine statusMachine;
    private final JobServiceClient jobServiceClient;
    private final CandidateServiceClient candidateServiceClient;
    private final NotificationClient notificationClient;

    /**
     * Creates a new application after cross-service validation:
     * 1. Validate candidate exists (via Candidate Service)
     * 2. Validate job exists and is OPEN (via Job Service)
     * 3. Check for duplicate application
     */
    @Transactional
    public Application createApplication(Long candidateId, Long jobId, String jobTitle, String candidateEmail) {
        if (candidateId == null || candidateId <= 0) {
            throw new IllegalArgumentException("Candidate ID must be a positive number");
        }
        if (jobId == null || jobId <= 0) {
            throw new IllegalArgumentException("Job ID must be a positive number");
        }

        log.info("Creating application for candidate: {} and job: {}", candidateId, jobId);

        // Priority 3: Cross-service validation
        candidateServiceClient.validateCandidateExists(candidateId);
        jobServiceClient.validateJobExistsAndOpen(jobId);

        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId)) {
            throw new RuntimeException("You have already applied for this job");
        }

        // Resolve job title from service if not provided
        String resolvedTitle = (jobTitle != null && !jobTitle.isBlank())
                ? jobTitle
                : jobServiceClient.getJobTitle(jobId);
        if (resolvedTitle == null) resolvedTitle = "Position #" + jobId;

        Application application = new Application();
        application.setCandidateId(candidateId);
        application.setJobId(jobId);
        application.setJobTitle(resolvedTitle);
        application.setCandidateEmail(candidateEmail);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setStatusChangedAt(LocalDateTime.now());
        application.setApplicationDate(LocalDateTime.now());
        application.setTrackingId(Application.generateTrackingId());

        Application saved = applicationRepository.save(application);
        log.info("Application {} created for candidate {} and job {}", saved.getId(), candidateId, jobId);
        return saved;
    }

    public ApplicationStatusResponse getApplicationStatus(String email, Long jobId) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid candidate email is required");
        }
        if (jobId == null || jobId <= 0) {
            throw new IllegalArgumentException("Valid job ID is required");
        }

        log.info("Getting status for email: {}, job: {}", email, jobId);

        List<Application> apps = applicationRepository.findByJobId(jobId);
        Application application = apps.stream()
                .filter(a -> email.equalsIgnoreCase(a.getCandidateEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Application not found for this email and job"));

        return buildStatusResponse(application);
    }

    public ApplicationStatusResponse getApplicationStatusByTrackingId(String trackingId) {
        if (trackingId == null || trackingId.isBlank()) {
            throw new IllegalArgumentException("Tracking ID cannot be blank");
        }

        log.info("Getting status for tracking ID: {}", trackingId);

        Application application = applicationRepository.findByTrackingId(trackingId.trim())
                .orElseThrow(() -> new RuntimeException("Application not found with Tracking ID: " + trackingId));

        return buildStatusResponse(application);
    }

    private ApplicationStatusResponse buildStatusResponse(Application application) {
        ApplicationStatusResponse response = new ApplicationStatusResponse();
        response.setApplicationId(application.getId());
        response.setTrackingId(application.getTrackingId());
        response.setStatus(application.getStatus().name());
        response.setJobTitle(application.getJobTitle() != null ? application.getJobTitle() : "Job #" + application.getJobId());
        response.setAppliedDate(application.getApplicationDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.setUpdatedAt(application.getStatusChangedAt() != null ?
                application.getStatusChangedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) :
                application.getApplicationDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    /**
     * Updates application status using State Machine validation.
     * Sends notification to candidate after every status change.
     * Priority 2: State Machine + Priority 4: Notifications
     */
    @Transactional
    public Application updateStatus(Long applicationId, ApplicationStatus newStatus, String reason) {
        if (applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException("Application ID must be a positive number");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        ApplicationStatus oldStatus = application.getStatus();

        // Priority 2: State Machine validation
        statusMachine.validateTransition(oldStatus, newStatus);

        application.setStatus(newStatus);
        application.setStatusChangedAt(LocalDateTime.now());

        if (newStatus == ApplicationStatus.HIRED) {
            application.setHiredDate(LocalDateTime.now());
            log.info("Candidate {} hired for application {}", application.getCandidateId(), applicationId);
        }

        if (newStatus == ApplicationStatus.REJECTED) {
            application.setRejectionReason(reason);
            log.info("Application {} rejected. Reason: {}", applicationId, reason);
        }

        Application saved = applicationRepository.save(application);

        // Priority 4: Send notification (non-fatal - failure does not rollback transaction)
        notificationClient.sendStatusChangeEmail(
                saved.getCandidateEmail(),
                applicationId,
                oldStatus,
                newStatus
        );

        return saved;
    }

    @Transactional
    public Application assignInterviewer(Long applicationId, Long interviewerId, LocalDateTime interviewDate) {
        if (applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException("Application ID must be a positive number");
        }
        if (interviewerId == null || interviewerId <= 0) {
            throw new IllegalArgumentException("Interviewer ID must be a positive number");
        }
        if (interviewDate == null) {
            throw new IllegalArgumentException("Interview date is required");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        ApplicationStatus oldStatus = application.getStatus();

        // Validate transition to INTERVIEW via State Machine
        statusMachine.validateTransition(oldStatus, ApplicationStatus.INTERVIEW);

        application.setInterviewerId(interviewerId);
        application.setInterviewDate(interviewDate);
        application.setStatus(ApplicationStatus.INTERVIEW);
        application.setStatusChangedAt(LocalDateTime.now());

        Application saved = applicationRepository.save(application);

        // Notify candidate about interview
        notificationClient.sendStatusChangeEmail(
                saved.getCandidateEmail(),
                applicationId,
                oldStatus,
                ApplicationStatus.INTERVIEW
        );

        return saved;
    }

    @Transactional
    public Application addEvaluation(Long applicationId, Double score, String notes) {
        if (applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException("Application ID must be a positive number");
        }
        if (score == null || score < 0.0 || score > 100.0) {
            throw new IllegalArgumentException("Evaluation score must be between 0.0 and 100.0");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        application.setEvaluationScore(score);
        application.setEvaluationNotes(notes);
        application.setStatusChangedAt(LocalDateTime.now());

        // Auto-promote to UNDER_REVIEW if score >= 70 and currently in INTERVIEW
        if (score >= 70 && application.getStatus() == ApplicationStatus.INTERVIEW
                && statusMachine.isValidTransition(ApplicationStatus.INTERVIEW, ApplicationStatus.UNDER_REVIEW)) {
            ApplicationStatus oldStatus = application.getStatus();
            application.setStatus(ApplicationStatus.UNDER_REVIEW);
            log.info("Application {} promoted to UNDER_REVIEW based on evaluation score {}", applicationId, score);

            Application saved = applicationRepository.save(application);
            notificationClient.sendStatusChangeEmail(
                    saved.getCandidateEmail(),
                    applicationId,
                    oldStatus,
                    ApplicationStatus.UNDER_REVIEW
            );
            return saved;
        }

        return applicationRepository.save(application);
    }

    @Transactional
    public Application updateApplicationJob(Long applicationId, Long newJobId, String newJobTitle) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + applicationId));

        application.setJobId(newJobId);
        if (newJobTitle != null) application.setJobTitle(newJobTitle);
        application.setStatusChangedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public List<Application> getApplicationsByCandidate(Long candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    public List<Application> getApplicationsByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public Application getApplicationById(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with ID: " + id));
    }

    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<Application> getApplicationsByMinScore(Double minScore) {
        return applicationRepository.findByEvaluationScoreGreaterThanEqual(minScore);
    }

    public Map<String, Object> getAdvancedStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalApplications", applicationRepository.count());
        stats.put("byStatus", applicationRepository.countGroupedByStatus());

        Double avgScore = applicationRepository.getAverageAiMatchScore();
        stats.put("averageAIScore", avgScore != null ? avgScore : 0.0);

        Double maxScore = applicationRepository.getMaxAiMatchScore();
        stats.put("maxAIScore", maxScore != null ? maxScore : 0.0);

        stats.put("applicationsLastWeek", applicationRepository.countByApplicationDateAfter(LocalDateTime.now().minusWeeks(1)));
        stats.put("applicationsLastMonth", applicationRepository.countByApplicationDateAfter(LocalDateTime.now().minusMonths(1)));

        Long hiredCount = applicationRepository.countByStatus(ApplicationStatus.HIRED);
        Long totalCount = applicationRepository.count();
        stats.put("hiringRate", totalCount > 0 ? (hiredCount * 100.0 / totalCount) : 0.0);

        return stats;
    }

    @Transactional
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }

    public List<Application> getPendingApplications() {
        return applicationRepository.findByStatusIn(
                List.of(ApplicationStatus.APPLIED, ApplicationStatus.INTERVIEW, ApplicationStatus.UNDER_REVIEW)
        );
    }
}
