package com.deepesh.resumeai.service;

import com.deepesh.resumeai.dto.ResumeAnalysisResponse;

public interface ResumeAnalyzer {
    ResumeAnalysisResponse analyze(String resumeText, String jobDescription);
}
