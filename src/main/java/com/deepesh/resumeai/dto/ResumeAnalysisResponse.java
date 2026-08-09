package com.deepesh.resumeai.dto;

import java.util.List;

public record ResumeAnalysisResponse(
        int matchScore,
        int atsScore,
        int confidenceScore,
        String hiringRecommendation,
        String executiveAssessment,
        List<String> matchedSkills,
        List<String> missingSkills,
        List<String> criticalGaps,
        List<String> preferredGaps,
        List<String> transferableSkills,
        List<String> strengths,
        List<String> weaknesses,
        List<String> atsRecommendations,
        List<String> resumeEvidence,
        String improvedSummary,
        List<String> improvedBullets,
        List<String> interviewFocusAreas,
        String analysisMode) {

    public ResumeAnalysisResponse {
        matchedSkills = safe(matchedSkills);
        missingSkills = safe(missingSkills);
        criticalGaps = safe(criticalGaps);
        preferredGaps = safe(preferredGaps);
        transferableSkills = safe(transferableSkills);
        strengths = safe(strengths);
        weaknesses = safe(weaknesses);
        atsRecommendations = safe(atsRecommendations);
        resumeEvidence = safe(resumeEvidence);
        improvedBullets = safe(improvedBullets);
        interviewFocusAreas = safe(interviewFocusAreas);

        hiringRecommendation =
                hiringRecommendation == null ? "" : hiringRecommendation;

        executiveAssessment =
                executiveAssessment == null ? "" : executiveAssessment;

        improvedSummary =
                improvedSummary == null ? "" : improvedSummary;

        analysisMode =
                analysisMode == null ? "" : analysisMode;
    }

    private static List<String> safe(List<String> value) {
        return value == null ? List.of() : List.copyOf(value);
    }

    public ResumeAnalysisResponse withAnalysisMode(String mode) {
        return new ResumeAnalysisResponse(
                matchScore,
                atsScore,
                confidenceScore,
                hiringRecommendation,
                executiveAssessment,
                matchedSkills,
                missingSkills,
                criticalGaps,
                preferredGaps,
                transferableSkills,
                strengths,
                weaknesses,
                atsRecommendations,
                resumeEvidence,
                improvedSummary,
                improvedBullets,
                interviewFocusAreas,
                mode);
    }
}