package com.services.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CvAnalysisResult {
    private String extractedText;
    private String extractedName;
    private String extractedEmail;
    private String extractedSkills;
    private Integer extractedExperienceYears;
    private String extractedQualification;
    private Double confidenceScore;
    private boolean isValid;
    private String validationNotes;
    private String rawText;
}