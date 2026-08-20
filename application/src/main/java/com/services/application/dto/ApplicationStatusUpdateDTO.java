package com.services.application.dto;

import com.services.application.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ApplicationStatusUpdateDTO {
    @NotNull(message = "Application status is required")
    private ApplicationStatus status;
    private String reason;
    private String aiRole;
}