package com.services.candidate.service;

import com.services.candidate.model.Candidate;
import com.services.candidate.repository.CandidateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateService {

    private final CandidateRepository candidateRepository;

    @Transactional
    public Candidate createCandidate(String firstName, String lastName, String email,
                                     String skills, Integer experienceYears,
                                     MultipartFile cvFile) throws Exception {

        validateCandidateInput(firstName, lastName, email, experienceYears);

        log.info("Creating candidate: {} {}", firstName, lastName);

        Candidate candidate = new Candidate();
        candidate.setFirstName(firstName.trim());
        candidate.setLastName(lastName.trim());
        candidate.setEmail(email.trim());
        candidate.setSkills(skills);
        candidate.setExperienceYears(experienceYears != null ? experienceYears : 0);
        candidate.setIsEmployed(false);
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setUpdatedAt(LocalDateTime.now());

        if (cvFile != null && !cvFile.isEmpty()) {
            validateCvFile(cvFile);
            candidate.setCvFileName(cvFile.getOriginalFilename());
        }

        return candidateRepository.save(candidate);
    }

    @Transactional
    public Candidate createCandidate(Candidate candidate) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate details cannot be null");
        }
        validateCandidateInput(candidate.getFirstName(), candidate.getLastName(), candidate.getEmail(), candidate.getExperienceYears());
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateRepository.save(candidate);
    }

    @Transactional
    public Candidate createCandidateWithJob(Candidate candidate, Long jobId) {
        if (candidate == null) {
            throw new IllegalArgumentException("Candidate details cannot be null");
        }
        validateCandidateInput(candidate.getFirstName(), candidate.getLastName(), candidate.getEmail(), candidate.getExperienceYears());
        candidate.setCreatedAt(LocalDateTime.now());
        candidate.setUpdatedAt(LocalDateTime.now());
        return candidateRepository.save(candidate);
    }

    private void validateCandidateInput(String firstName, String lastName, String email, Integer experienceYears) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Candidate first name is required and cannot be blank");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Candidate last name is required and cannot be blank");
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new IllegalArgumentException("Valid candidate email is required");
        }
        if (experienceYears != null && experienceYears < 0) {
            throw new IllegalArgumentException("Experience years cannot be negative");
        }
    }

    private void validateCvFile(MultipartFile file) {
        if (file.getSize() > 15 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum allowed limit of 15MB");
        }
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (!lower.endsWith(".pdf") && !lower.endsWith(".doc") && !lower.endsWith(".docx") && !lower.endsWith(".txt") && !lower.endsWith(".rtf")) {
                throw new IllegalArgumentException("Invalid file format. Allowed formats: PDF, DOC, DOCX, TXT, RTF");
            }
        }
    }

    @Transactional
    public Candidate updateCandidate(Long id, String firstName, String lastName,
                                     String email, String skills, Integer experienceYears) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));

        if (firstName != null) candidate.setFirstName(firstName);
        if (lastName != null) candidate.setLastName(lastName);
        if (email != null) candidate.setEmail(email);
        if (skills != null) candidate.setSkills(skills);
        if (experienceYears != null) candidate.setExperienceYears(experienceYears);
        candidate.setUpdatedAt(LocalDateTime.now());

        return candidateRepository.save(candidate);
    }

    @Transactional
    public void deleteCandidate(Long id) {
        Candidate candidate = candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));
        candidateRepository.delete(candidate);
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }

    public Candidate getCandidateById(Long id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidate not found with id: " + id));
    }

    public Candidate getCandidateByEmail(String email) {
        return candidateRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Candidate not found with email: " + email));
    }

    public List<Candidate> getEmployedCandidates() {
        return candidateRepository.findByIsEmployedTrue();
    }

    public List<Candidate> getUnemployedCandidates() {
        return candidateRepository.findByIsEmployedFalse();
    }

    public List<Candidate> searchCandidatesBySkills(String skillKeyword) {
        return candidateRepository.findBySkillsContainingIgnoreCase(skillKeyword);
    }

    public long countCandidates() {
        return candidateRepository.count();
    }

    public long countEmployedCandidates() {
        return candidateRepository.countByIsEmployedTrue();
    }
}