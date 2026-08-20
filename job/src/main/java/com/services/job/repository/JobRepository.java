package com.services.job.repository;

import com.services.job.model.Job;
import com.services.job.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);

    List<Job> findByTitleContainingIgnoreCase(String title);

    List<Job> findByDepartment(String department);

    List<Job> findByEmploymentType(String employmentType);

    List<Job> findByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT j FROM Job j WHERE j.status = com.services.job.enums.JobStatus.OPEN AND (LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.requirements) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Job> searchOpenJobs(@Param("keyword") String keyword);

    long countByStatus(JobStatus status);

    List<Job> findByStatusAndUpdatedAtBefore(JobStatus status, LocalDateTime dateTime);
}