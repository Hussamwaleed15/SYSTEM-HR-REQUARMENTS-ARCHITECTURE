package com.services.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiValidationResult {
    private boolean validated;
    private Double confidence;
    private String notes;
}