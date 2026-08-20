package com.services.job.controller;

import com.services.job.dto.JobCreateDTO;
import com.services.job.model.Job;
import com.services.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<?> createJob(@Valid @RequestBody JobCreateDTO dto) {
        try {
            Job job = new Job();
            job.setTitle(dto.getTitle());
            job.setDescription(dto.getDescription());
            job.setRequirements(dto.getRequirements());
            job.setStatus(dto.getStatus());
            job.setLocation(dto.getLocation());
            job.setDepartment(dto.getDepartment());
            job.setEmploymentType(dto.getEmploymentType());

            Job saved = jobService.createJob(job);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job created successfully");
            response.put("jobId", saved.getId());
            response.put("data", saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Job>> getAllJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/open")
    public ResponseEntity<List<Job>> getOpenJobs() {
        return ResponseEntity.ok(jobService.getOpenJobs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateJob(@PathVariable Long id, @RequestBody Job jobDetails) {
        try {
            Job updated = jobService.updateJob(id, jobDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job updated successfully");
            response.put("jobId", id);
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try {
            jobService.deleteJob(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job deleted successfully");
            response.put("jobId", id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateJobStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Job updated = jobService.updateJobStatus(id, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Job status updated successfully");
            response.put("jobId", id);
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}