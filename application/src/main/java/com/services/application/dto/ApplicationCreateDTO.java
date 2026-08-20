package com.services.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreateDTO {

    @NotNull(message = "Candidate ID is required")
    @Positive(message = "Candidate ID must be a positive number")
    private Long candidateId;

    @NotNull(message = "Job ID is required")
    @Positive(message = "Job ID must be a positive number")
    private Long jobId;

    private String jobTitle;

    @Email(message = "Invalid candidate email format")
    private String candidateEmail;
}