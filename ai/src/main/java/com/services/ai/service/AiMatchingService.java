package com.services.ai.service;

import com.services.ai.dto.JobMatchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AiMatchingService {

    public JobMatchResult evaluateMatch(String candidateSkills, Integer candidateExperienceYears,
                                        String jobTitle, String jobRequirements) {
        int candidateExperience = candidateExperienceYears != null ? candidateExperienceYears : 0;
        int requiredExperience = extractRequiredExperience(jobRequirements);

        double matchScore = 0.0;
        int matchedSkills = 0;
        String[] skills = new String[0];
        String[] requiredSkillsArray = new String[0];

        if (candidateSkills != null && !candidateSkills.isEmpty()) {
            skills = candidateSkills.split(",");
        }

        if (jobRequirements != null && !jobRequirements.isEmpty()) {
            requiredSkillsArray = extractSkillsFromRequirements(jobRequirements);
        }

        if (skills.length > 0 && requiredSkillsArray.length > 0) {
            for (String skill : skills) {
                String skillTrimmed = skill.trim().toLowerCase();
                for (String req : requiredSkillsArray) {
                    if (req.toLowerCase().contains(skillTrimmed) || skillTrimmed.contains(req.toLowerCase())) {
                        matchedSkills++;
                        break;
                    }
                }
            }
            double skillMatch = (skills.length > 0) ? (matchedSkills * 100.0 / skills.length) : 0.0;
            matchScore += skillMatch * 0.6;
        }

        if (candidateExperience >= requiredExperience) {
            matchScore += 30.0;
        } else if (candidateExperience >= requiredExperience - 2) {
            matchScore += 15.0;
        } else if (candidateExperience >= requiredExperience - 4) {
            matchScore += 7.0;
        }

        double titleMatch = 0.0;
        if (jobTitle != null && candidateSkills != null) {
            String[] titleKeywords = jobTitle.toLowerCase().split(" ");
            for (String keyword : titleKeywords) {
                if (keyword.length() > 2 && candidateSkills.toLowerCase().contains(keyword)) {
                    titleMatch = 10.0;
                    break;
                }
            }
        }
        matchScore += titleMatch;

        String matchLevel;
        if (matchScore >= 80) {
            matchLevel = "Excellent";
        } else if (matchScore >= 60) {
            matchLevel = "Good";
        } else if (matchScore >= 40) {
            matchLevel = "Moderate";
        } else {
            matchLevel = "Low";
        }

        String summary = generateSummary(matchLevel, matchScore, matchedSkills, skills.length,
                candidateExperience, requiredExperience, jobTitle);

        String details = String.format(
                "Skills: %d/%d matched. Experience: %d years (required: %d+). Title match: %s",
                matchedSkills, skills.length, candidateExperience, requiredExperience,
                titleMatch > 0 ? "Yes" : "No"
        );

        return new JobMatchResult(
                matchScore,
                matchLevel,
                summary,
                matchedSkills,
                skills.length,
                candidateExperience,
                requiredExperience,
                details
        );
    }

    private String[] extractSkillsFromRequirements(String requirements) {
        if (requirements == null) return new String[0];
        String clean = requirements.replaceAll("\\s+", " ");
        return clean.split("[,\\.;]|(?:and|or|with)\\s+");
    }

    private int extractRequiredExperience(String requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return 0;
        }
        Pattern pattern = Pattern.compile("(\\d+)\\s*\\+?\\s*(?:years|year|yrs|yr)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(requirements);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private String generateSummary(String level, double score, int matched, int total,
                                   int exp, int reqExp, String title) {
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("%s fit (%.1f%% match). ", level, score));

        switch (level) {
            case "Excellent":
                summary.append("Perfect candidate for the role.");
                break;
            case "Good":
                summary.append("Good fit with minor gaps.");
                break;
            case "Moderate":
                summary.append("Moderate fit with some gaps that need attention.");
                break;
            case "Low":
                summary.append("Low fit. Review required.");
                break;
        }

        return summary.toString();
    }
}