package com.services.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMatchRequest {

    @NotBlank(message = "Candidate skills cannot be blank")
    private String candidateSkills;

    private Integer candidateExperienceYears;

    private String jobTitle;

    @NotBlank(message = "Job requirements cannot be blank")
    private String jobRequirements;
}