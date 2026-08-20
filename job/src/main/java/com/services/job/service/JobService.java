package com.services.job.service;

import com.services.job.model.Job;
import com.services.job.repository.JobRepository;
import com.services.job.enums.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;

    @Transactional
    public Job createJob(Job job) {
        if (job == null) {
            throw new IllegalArgumentException("Job details cannot be null");
        }
        if (job.getTitle() == null || job.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title is required and cannot be blank");
        }
        if (job.getDescription() == null || job.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Job description is required and cannot be blank");
        }

        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        if (job.getStatus() == null) {
            job.setStatus(JobStatus.OPEN);
        }
        return jobRepository.save(job);
    }

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Job getJobById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid job ID: " + id);
        }
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with ID: " + id));
    }

    public List<Job> getOpenJobs() {
        return jobRepository.findByStatus(JobStatus.OPEN);
    }

    @Transactional
    public Job updateJob(Long id, Job jobDetails) {
        Job job = getJobById(id);
        if (jobDetails.getTitle() != null && !jobDetails.getTitle().trim().isEmpty()) {
            job.setTitle(jobDetails.getTitle());
        }
        if (jobDetails.getDescription() != null && !jobDetails.getDescription().trim().isEmpty()) {
            job.setDescription(jobDetails.getDescription());
        }
        if (jobDetails.getRequirements() != null) {
            job.setRequirements(jobDetails.getRequirements());
        }
        if (jobDetails.getStatus() != null) {
            job.setStatus(jobDetails.getStatus());
        }
        if (jobDetails.getLocation() != null) {
            job.setLocation(jobDetails.getLocation());
        }
        if (jobDetails.getDepartment() != null) {
            job.setDepartment(jobDetails.getDepartment());
        }
        if (jobDetails.getEmploymentType() != null) {
            job.setEmploymentType(jobDetails.getEmploymentType());
        }
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJobStatus(Long id, String status) {
        Job job = getJobById(id);
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        try {
            job.setStatus(JobStatus.valueOf(status.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid job status: '" + status + "'. Allowed values are: OPEN, CLOSED, DRAFT, ON_HOLD, CANCELLED");
        }
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long id) {
        Job job = getJobById(id);
        jobRepository.delete(job);
    }
}