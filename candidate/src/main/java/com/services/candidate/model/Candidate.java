package com.services.candidate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_seq")
    @SequenceGenerator(name = "candidate_seq", sequenceName = "CANDIDATE_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(columnDefinition = "CLOB")
    private String skills;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "current_position", length = 100)
    private String currentPosition;

    @Column(name = "current_company", length = 100)
    private String currentCompany;

    @Column(name = "cv_file_name", length = 255)
    private String cvFileName;

    @Column(name = "cv_file_path", length = 500)
    private String cvFilePath;

    @Column(name = "is_employed")
    private Boolean isEmployed = false;

    @Column(name = "ai_validated")
    private Boolean aiValidated = false;

    @Column(name = "ai_validation_notes", columnDefinition = "CLOB")
    private String aiValidationNotes;

    @Column(name = "ai_confidence_score", columnDefinition = "NUMBER")
    private Double aiConfidenceScore;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}