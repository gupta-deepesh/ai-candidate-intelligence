package com.deepesh.resumeai.service;

import com.deepesh.resumeai.dto.ResumeAnalysisResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockResumeAnalyzerTest {

    private final MockResumeAnalyzer analyzer = new MockResumeAnalyzer();

    @Test
    void detectsMatchedAndMissingSkills() {
        String resume =
                "Principal Engineer with Java, Spring Boot, AWS and Terraform experience.";
        String jd =
                "Looking for Java, Spring Boot, AWS, Terraform and Kubernetes experience.";

        ResumeAnalysisResponse response = analyzer.analyze(resume, jd);

        assertThat(response.matchedSkills())
                .contains("Java", "Spring boot", "AWS", "Terraform");
        assertThat(response.missingSkills()).contains("Kubernetes");
        assertThat(response.matchScore()).isBetween(0, 100);
        assertThat(response.atsScore()).isBetween(0, 100);
        assertThat(response.analysisMode()).isEqualTo("MOCK");
    }
}
