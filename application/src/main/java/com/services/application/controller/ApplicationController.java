package com.services.application.controller;

import com.services.application.dto.ApplicationCreateDTO;
import com.services.application.dto.ApplicationResponse;
import com.services.application.dto.ApplicationStatusResponse;
import com.services.application.dto.ApplicationStatusUpdateDTO;
import com.services.application.model.Application;
import com.services.application.service.ApplicationService;
import com.services.application.enums.ApplicationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/public/status")
    public ResponseEntity<?> getPublicStatus(
            @RequestParam String email,
            @RequestParam Long jobId) {

        try {
            ApplicationStatusResponse response = applicationService.getApplicationStatus(email, jobId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/public/status/tracking")
    public ResponseEntity<?> getStatusByTrackingId(@RequestParam String trackingId) {
        try {
            ApplicationStatusResponse response = applicationService.getApplicationStatusByTrackingId(trackingId);

            return ResponseEntity.ok()
                    .header("X-Tracking-ID", response.getTrackingId())
                    .body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> createApplicationFromJson(@Valid @RequestBody ApplicationCreateDTO dto) {
        try {
            Application application = applicationService.createApplication(
                    dto.getCandidateId(),
                    dto.getJobId(),
                    dto.getJobTitle(),
                    dto.getCandidateEmail()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Application created successfully");
            response.put("applicationId", application.getId());
            response.put("trackingId", application.getTrackingId());
            response.put("data", application);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("X-Tracking-ID", application.getTrackingId())
                    .header("X-Application-ID", String.valueOf(application.getId()))
                    .body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createApplication(
            @RequestParam Long candidateId,
            @RequestParam Long jobId,
            @RequestParam(required = false) String jobTitle,
            @RequestParam(required = false) String candidateEmail) {

        try {
            Application application = applicationService.createApplication(candidateId, jobId, jobTitle, candidateEmail);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Application created successfully");
            response.put("applicationId", application.getId());
            response.put("trackingId", application.getTrackingId());
            response.put("data", application);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .header("X-Tracking-ID", application.getTrackingId())
                    .header("X-Application-ID", String.valueOf(application.getId()))
                    .body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}/status", consumes = "application/json")
    public ResponseEntity<?> updateStatusFromJson(
            @PathVariable Long id,
            @RequestBody ApplicationStatusUpdateDTO dto) {

        try {
            Application updated = applicationService.updateStatus(id, dto.getStatus(), dto.getReason());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Application status updated successfully");
            response.put("applicationId", id);
            response.put("status", updated.getStatus());
            response.put("trackingId", updated.getTrackingId());
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String reason) {

        try {
            Application updated = applicationService.updateStatus(id, status, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Application status updated successfully");
            response.put("applicationId", id);
            response.put("status", updated.getStatus());
            response.put("trackingId", updated.getTrackingId());
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/assign-interviewer")
    public ResponseEntity<?> assignInterviewer(
            @PathVariable Long id,
            @RequestParam Long interviewerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime interviewDate) {

        try {
            Application updated = applicationService.assignInterviewer(id, interviewerId, interviewDate);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Interview assigned successfully");
            response.put("applicationId", id);
            response.put("trackingId", updated.getTrackingId());
            response.put("interviewerId", interviewerId);
            response.put("interviewDate", interviewDate);
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/evaluation")
    public ResponseEntity<?> addEvaluation(
            @PathVariable Long id,
            @RequestParam Double score,
            @RequestParam(required = false) String notes) {

        try {
            Application updated = applicationService.addEvaluation(id, score, notes);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Evaluation added successfully");
            response.put("applicationId", id);
            response.put("trackingId", updated.getTrackingId());
            response.put("score", score);
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applicationService.getAllApplications();
        applications.sort((a1, a2) -> a2.getApplicationDate().compareTo(a1.getApplicationDate()));
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<Application>> getByCandidate(@PathVariable Long candidateId) {
        List<Application> applications = applicationService.getApplicationsByCandidate(candidateId);
        applications.sort((a1, a2) -> a2.getApplicationDate().compareTo(a1.getApplicationDate()));
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<List<Application>> getByJob(@PathVariable Long jobId) {
        List<Application> applications = applicationService.getApplicationsByJob(jobId);
        applications.sort((a1, a2) -> a2.getApplicationDate().compareTo(a1.getApplicationDate()));
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Application>> getByStatus(@PathVariable ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.getApplicationsByStatus(status));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(applicationService.getAdvancedStatistics());
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<Application>> getTopRated(@RequestParam(defaultValue = "70") Double minScore) {
        return ResponseEntity.ok(applicationService.getApplicationsByMinScore(minScore));
    }

    @PutMapping("/{id}/hire")
    public ResponseEntity<?> hireCandidate(@PathVariable Long id) {
        try {
            Application updated = applicationService.updateStatus(id, ApplicationStatus.HIRED, "Candidate hired");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate hired successfully");
            response.put("applicationId", id);
            response.put("trackingId", updated.getTrackingId());
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectCandidate(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        try {
            String rejectionReason = reason != null ? reason : "Candidate not selected";
            Application updated = applicationService.updateStatus(id, ApplicationStatus.REJECTED, rejectionReason);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate rejected successfully");
            response.put("applicationId", id);
            response.put("trackingId", updated.getTrackingId());
            response.put("reason", rejectionReason);
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id) {
        try {
            applicationService.deleteApplication(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Application deleted successfully");
            response.put("applicationId", id);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/job")
    public ResponseEntity<?> updateApplicationJob(
            @PathVariable Long id,
            @RequestParam Long newJobId,
            @RequestParam(required = false) String newJobTitle) {

        try {
            Application updated = applicationService.updateApplicationJob(id, newJobId, newJobTitle);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Application job updated successfully");
            response.put("applicationId", id);
            response.put("trackingId", updated.getTrackingId());
            response.put("newJobId", newJobId);
            response.put("data", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}