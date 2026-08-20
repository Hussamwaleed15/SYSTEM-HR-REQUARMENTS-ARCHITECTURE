package com.services.candidate.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CandidateDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer age;
    private String education;
    private String skills;
    private Integer experienceYears;
    private String cvPath;
    private Boolean aiValidated;
    private String aiValidationNotes;
    private Boolean isEmployed;
    private LocalDateTime createdAt;
}