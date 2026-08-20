package com.services.application.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Client for Job Service cross-service validation.
 * Verifies that a job exists and is in OPEN status before allowing application creation.
 */
@Component
@Slf4j
public class JobServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.job-service.url}")
    private String jobServiceUrl;

    public JobServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Checks whether a job with the given ID exists and is OPEN.
     * Throws RuntimeException if job not found or not open.
     */
    public void validateJobExistsAndOpen(Long jobId) {
        String url = jobServiceUrl + "/api/jobs/" + jobId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> job = restTemplate.getForObject(url, Map.class);
            if (job == null) {
                throw new RuntimeException("Job not found with ID: " + jobId);
            }
            Object status = job.get("status");
            if (!"OPEN".equalsIgnoreCase(String.valueOf(status))) {
                throw new RuntimeException(
                        "Job with ID " + jobId + " is not open for applications. Current status: " + status);
            }
            log.info("Job {} validated: exists and is OPEN", jobId);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Job not found with ID: " + jobId);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to validate job {}: {}", jobId, e.getMessage());
            throw new RuntimeException("Unable to verify job availability. Please try again later.");
        }
    }

    /**
     * Fetches job title for a given job ID. Returns null if not found.
     */
    public String getJobTitle(Long jobId) {
        String url = jobServiceUrl + "/api/jobs/" + jobId;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> job = restTemplate.getForObject(url, Map.class);
            return job != null ? String.valueOf(job.getOrDefault("title", "Position #" + jobId)) : null;
        } catch (Exception e) {
            log.warn("Could not fetch job title for {}: {}", jobId, e.getMessage());
            return null;
        }
    }
}
