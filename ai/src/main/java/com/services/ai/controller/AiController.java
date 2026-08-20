package com.services.ai.controller;

import com.services.ai.dto.CvAnalysisResult;
import com.services.ai.dto.JobMatchRequest;
import com.services.ai.dto.JobMatchResult;
import com.services.ai.service.AiMatchingService;
import com.services.ai.service.CvParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final CvParserService cvParserService;
    private final AiMatchingService aiMatchingService;

    @PostMapping("/parse-cv")
    public ResponseEntity<CvAnalysisResult> parseCv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "skills", required = false) String skills,
            @RequestParam(value = "experienceYears", required = false) Integer experienceYears) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getSize() > 15 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 15MB limit");
        }

        CvAnalysisResult result = cvParserService.analyzeCv(
                file.getBytes(), file.getOriginalFilename(), name, email, skills, experienceYears
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/match")
    public ResponseEntity<JobMatchResult> matchCandidate(@Valid @RequestBody JobMatchRequest request) {
        JobMatchResult result = aiMatchingService.evaluateMatch(
                request.getCandidateSkills(),
                request.getCandidateExperienceYears(),
                request.getJobTitle(),
                request.getJobRequirements()
        );
        return ResponseEntity.ok(result);
    }
}