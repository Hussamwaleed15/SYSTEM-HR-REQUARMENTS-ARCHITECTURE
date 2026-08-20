package com.services.candidate.controller;

import com.services.candidate.dto.CandidateCreateDTO;
import com.services.candidate.model.Candidate;
import com.services.candidate.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Slf4j
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createCandidateJson(@Valid @RequestBody CandidateCreateDTO dto) {
        try {
            Candidate candidate = new Candidate();
            candidate.setFirstName(dto.getFirstName());
            candidate.setLastName(dto.getLastName());
            candidate.setEmail(dto.getEmail());
            candidate.setSkills(dto.getSkills());
            candidate.setExperienceYears(dto.getExperienceYears());

            Candidate saved = candidateService.createCandidateWithJob(candidate, dto.getJobId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate created successfully");
            response.put("candidateId", saved.getId());
            response.put("data", saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCandidateMultipart(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "skills", required = false) String skills,
            @RequestParam(value = "experienceYears", required = false) Integer experienceYears,
            @RequestParam(value = "jobId", required = false) Long jobId,
            @RequestParam(value = "cvFile", required = false) MultipartFile cvFile,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            MultipartFile actualFile = cvFile != null ? cvFile : file;
            Candidate saved = candidateService.createCandidate(firstName, lastName, email, skills, experienceYears, actualFile);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate created successfully");
            response.put("candidateId", saved.getId());
            response.put("data", saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCandidate(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(required = false) Long jobId,
            @RequestBody(required = false) CandidateCreateDTO dto) {
        try {
            String fName = dto != null && dto.getFirstName() != null ? dto.getFirstName() : firstName;
            String lName = dto != null && dto.getLastName() != null ? dto.getLastName() : lastName;
            String em = dto != null && dto.getEmail() != null ? dto.getEmail() : email;
            String sk = dto != null && dto.getSkills() != null ? dto.getSkills() : skills;
            Integer exp = dto != null && dto.getExperienceYears() != null ? dto.getExperienceYears() : experienceYears;
            Long jId = dto != null && dto.getJobId() != null ? dto.getJobId() : jobId;

            Candidate candidate = new Candidate();
            candidate.setFirstName(fName);
            candidate.setLastName(lName);
            candidate.setEmail(em);
            candidate.setSkills(sk);
            candidate.setExperienceYears(exp);

            Candidate saved = candidateService.createCandidateWithJob(candidate, jId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate created successfully");
            response.put("candidateId", saved.getId());
            response.put("data", saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Candidate>> getAllCandidates() {
        return ResponseEntity.ok(candidateService.getAllCandidates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {
        return ResponseEntity.ok(candidateService.getCandidateById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Candidate> getCandidateByEmail(@PathVariable String email) {
        return ResponseEntity.ok(candidateService.getCandidateByEmail(email));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateCandidateJson(
            @PathVariable Long id,
            @RequestBody CandidateCreateDTO dto) {
        try {
            Candidate updated = candidateService.updateCandidate(
                    id,
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.getEmail(),
                    dto.getSkills(),
                    dto.getExperienceYears()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate updated successfully");
            response.put("candidateId", id);
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCandidate(
            @PathVariable Long id,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Integer experienceYears) {
        try {
            Candidate updated = candidateService.updateCandidate(id, firstName, lastName, email, skills, experienceYears);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate updated successfully");
            response.put("candidateId", id);
            response.put("data", updated);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCandidate(@PathVariable Long id) {
        try {
            candidateService.deleteCandidate(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Candidate deleted successfully");
            response.put("candidateId", id);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}