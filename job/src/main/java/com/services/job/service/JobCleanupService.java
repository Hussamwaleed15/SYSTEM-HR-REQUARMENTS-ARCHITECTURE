package com.services.job.service;

import com.services.job.model.Job;
import com.services.job.repository.JobRepository;
import com.services.job.enums.JobStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobCleanupService {

    private final JobRepository jobRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void deleteOldClosedJobs() {
        log.info("Starting cleanup of old closed jobs...");
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Job> oldJobs = jobRepository.findByStatusAndUpdatedAtBefore(JobStatus.CLOSED, thirtyDaysAgo);

        if (oldJobs.isEmpty()) {
            log.info("No old closed jobs to delete.");
            return;
        }

        jobRepository.deleteAll(oldJobs);
        log.info("Deleted {} old closed jobs.", oldJobs.size());
    }
}