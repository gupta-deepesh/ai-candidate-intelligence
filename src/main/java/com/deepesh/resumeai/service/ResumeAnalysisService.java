package com.deepesh.resumeai.service;

import com.deepesh.resumeai.dto.ResumeAnalysisResponse;
import com.deepesh.resumeai.exception.InvalidResumeException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeAnalysisService {

    private static final int MAX_JOB_DESCRIPTION_CHARACTERS = 25_000;

    private final ResumeParserService resumeParserService;
    private final ResumeAnalyzer resumeAnalyzer;

    public ResumeAnalysisService(
            ResumeParserService resumeParserService,
            ResumeAnalyzer resumeAnalyzer) {
        this.resumeParserService = resumeParserService;
        this.resumeAnalyzer = resumeAnalyzer;
    }

    public ResumeAnalysisResponse analyze(
            MultipartFile resume,
            String jobDescription) {

        if (jobDescription == null || jobDescription.isBlank()) {
            throw new InvalidResumeException("Job description is required.");
        }

        String resumeText = resumeParserService.extractText(resume);
        String jd = jobDescription.trim();

        if (jd.length() > MAX_JOB_DESCRIPTION_CHARACTERS) {
            jd = jd.substring(0, MAX_JOB_DESCRIPTION_CHARACTERS);
        }

        return resumeAnalyzer.analyze(resumeText, jd);
    }
}
