package com.services.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchResult {
    private double matchPercentage;
    private String matchLevel;      // "Excellent", "Good", "Moderate", "Low"
    private String summary;
    private int matchedSkills;
    private int totalRequiredSkills;
    private int candidateExperience;
    private int requiredExperience;
    private String details;
}