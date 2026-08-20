package com.services.job.dto;

import com.services.job.enums.JobStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class JobDTO {
    private Long id;
    private String title;
    private String department;
    private String description;
    private String requirements;
    private Integer slots;
    private LocalDate deadline;
    private JobStatus status;
    private String createdByUsername;
    private LocalDateTime createdAt;
}