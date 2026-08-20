package com.services.ai.service;

import com.services.ai.dto.CvAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class CvParserService {

    private final TextExtractorService textExtractorService;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^(?:Name|Full Name|Candidate)\\s*[:|]?\\s*(.+)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
    private static final Pattern SKILLS_PATTERN =
            Pattern.compile("(?:Skills|Technical Skills|Core Competencies)\\s*[:|]?\\s*(.+?)(?=\\n\\s*\\n|\\Z)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private static final Pattern EXPERIENCE_PATTERN =
            Pattern.compile("(\\d+)\\s*\\+?\\s*(?:years|year|yrs|yr)\\s*(?:of)?\\s*(?:experience)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUALIFICATION_PATTERN =
            Pattern.compile("(?:Education|Qualification|Degree)\\s*[:|]?\\s*(.+?)(?=\\n\\s*\\n|\\Z)", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public CvAnalysisResult analyzeCv(byte[] fileBytes, String fileName, String providedName,
                                      String providedEmail, String providedSkills,
                                      Integer providedExperienceYears) throws Exception {

        log.info("Analyzing CV: {}, for candidate: {}", fileName, providedName);

        String extractedText = textExtractorService.extractTextFromBytes(fileBytes, fileName);
        log.info("Extracted text length: {} characters", extractedText.length());

        if (extractedText.isEmpty() || extractedText.length() < 10) {
            return new CvAnalysisResult(
                    extractedText,
                    null, null, null, null, null,
                    0.0, false,
                    "Failed to extract text from CV",
                    extractedText
            );
        }

        String extractedName = extractName(extractedText);
        String extractedEmail = extractEmail(extractedText);
        String extractedSkills = extractSkills(extractedText);
        Integer extractedExperience = extractExperience(extractedText);
        String extractedQualification = extractQualification(extractedText);

        double confidence = calculateConfidence(
                providedName, extractedName,
                providedEmail, extractedEmail,
                providedSkills, extractedSkills,
                providedExperienceYears, extractedExperience
        );

        boolean isValid = confidence >= 60.0;
        String notes = generateValidationNotes(confidence, providedName, extractedName,
                providedEmail, extractedEmail, providedSkills, extractedSkills);

        return new CvAnalysisResult(
                extractedText,
                extractedName,
                extractedEmail,
                extractedSkills,
                extractedExperience,
                extractedQualification,
                confidence,
                isValid,
                notes,
                extractedText.length() > 500 ? extractedText.substring(0, 500) + "..." : extractedText
        );
    }

    private String extractName(String text) {
        Matcher matcher = NAME_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        String[] lines = text.split("\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 2 && line.length() < 50 && !line.contains("@")) {
                return line;
            }
        }
        return null;
    }

    private String extractEmail(String text) {
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractSkills(String text) {
        Matcher matcher = SKILLS_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim().replaceAll("\\s+", " ");
        }
        return null;
    }

    private Integer extractExperience(String text) {
        Matcher matcher = EXPERIENCE_PATTERN.matcher(text);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String extractQualification(String text) {
        Matcher matcher = QUALIFICATION_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    private double calculateConfidence(String providedName, String extractedName,
                                       String providedEmail, String extractedEmail,
                                       String providedSkills, String extractedSkills,
                                       Integer providedExperience, Integer extractedExperience) {

        double score = 0.0;
        int totalChecks = 0;

        if (providedName != null && extractedName != null) {
            totalChecks++;
            if (providedName.trim().equalsIgnoreCase(extractedName.trim())) {
                score += 100;
            } else if (extractedName.toLowerCase().contains(providedName.toLowerCase())) {
                score += 70;
            } else {
                score += 30;
            }
        }

        if (providedEmail != null && extractedEmail != null) {
            totalChecks++;
            if (providedEmail.trim().equalsIgnoreCase(extractedEmail.trim())) {
                score += 100;
            } else {
                score += 30;
            }
        }

        if (providedSkills != null && extractedSkills != null) {
            totalChecks++;
            String[] provided = providedSkills.split(",");
            String[] extracted = extractedSkills.split(",");
            int matched = 0;
            for (String p : provided) {
                for (String e : extracted) {
                    if (e.trim().toLowerCase().contains(p.trim().toLowerCase())) {
                        matched++;
                        break;
                    }
                }
            }
            score += (provided.length > 0) ? (matched * 100.0 / provided.length) : 0;
        }

        if (providedExperience != null && extractedExperience != null) {
            totalChecks++;
            if (Math.abs(providedExperience - extractedExperience) <= 1) {
                score += 100;
            } else if (Math.abs(providedExperience - extractedExperience) <= 3) {
                score += 70;
            } else {
                score += 40;
            }
        }

        return totalChecks > 0 ? score / totalChecks : 0.0;
    }

    private String generateValidationNotes(double confidence, String providedName, String extractedName,
                                           String providedEmail, String extractedEmail,
                                           String providedSkills, String extractedSkills) {
        StringBuilder notes = new StringBuilder();
        notes.append(String.format("AI Validation: %.1f%% confidence. ", confidence));

        if (confidence >= 80) {
            notes.append("✅ All data validated successfully.");
        } else if (confidence >= 60) {
            notes.append("⚠️ Data partially validated.");
        } else {
            notes.append("❌ Validation failed.");
        }

        if (providedName != null && extractedName != null && !providedName.equalsIgnoreCase(extractedName)) {
            notes.append(" Name mismatch.");
        }
        if (providedEmail != null && extractedEmail != null && !providedEmail.equalsIgnoreCase(extractedEmail)) {
            notes.append(" Email mismatch.");
        }

        return notes.toString();
    }
}