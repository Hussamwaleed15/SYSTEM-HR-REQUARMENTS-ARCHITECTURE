package com.services.application.model;

import com.services.application.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "application_seq")
    @SequenceGenerator(name = "application_seq", sequenceName = "APPLICATION_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "tracking_id", unique = true, nullable = false, length = 36)
    private String trackingId;

    @Column(name = "candidate_id", nullable = false)
    private Long candidateId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "candidate_email")
    private String candidateEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "interviewer_id")
    private Long interviewerId;

    @Column(name = "interview_date")
    private LocalDateTime interviewDate;

    @Column(name = "evaluation_score", columnDefinition = "NUMBER")
    private Double evaluationScore;

    @Column(name = "evaluation_notes", columnDefinition = "CLOB")
    private String evaluationNotes;

    @Column(name = "ai_role", columnDefinition = "CLOB")
    private String aiRole;

    @Column(name = "ai_match_score", columnDefinition = "NUMBER")
    private Double aiMatchScore;

    @Column(name = "ai_match_level")
    private String aiMatchLevel;

    @Column(name = "rejection_reason", columnDefinition = "CLOB")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "application_date", updatable = false)
    private LocalDateTime applicationDate;

    @Column(name = "status_changed_at")
    private LocalDateTime statusChangedAt;

    @Column(name = "hired_date")
    private LocalDateTime hiredDate;

    public static String generateTrackingId() {
        return UUID.randomUUID().toString();
    }
}