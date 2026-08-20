package com.services.application.enums;

public enum ApplicationStatus {
    APPLIED,        // Initial state when candidate applies
    INTERVIEW,      // Interviewer assigned - interview stage
    UNDER_REVIEW,   // Post-interview evaluation phase
    OFFER_EXTENDED, // Offer has been made to the candidate
    HIRED,          // Candidate accepted and hired (terminal)
    REJECTED,       // Application rejected (terminal)
    WITHDRAWN       // Candidate withdrew the application (terminal)
}