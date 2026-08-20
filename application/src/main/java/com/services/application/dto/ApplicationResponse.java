package com.services.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long applicationId;
    private String trackingId;
    private String status;
    private AiValidationResult aiValidation;
    private AiMatchingResult aiMatching;
}