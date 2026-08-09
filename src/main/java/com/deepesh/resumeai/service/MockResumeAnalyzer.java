package com.deepesh.resumeai.service;

import com.deepesh.resumeai.dto.ResumeAnalysisResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@ConditionalOnProperty(
        name = "app.ai.mock",
        havingValue = "true",
        matchIfMissing = true)
public class MockResumeAnalyzer implements ResumeAnalyzer {

    private static final Set<String> TRACKED_SKILLS = Set.of(
            "java",
            "spring boot",
            "microservices",
            "aws",
            "lambda",
            "ecs",
            "fargate",
            "terraform",
            "docker",
            "kubernetes",
            "eks",
            "kafka",
            "redis",
            "sql",
            "rest",
            "oauth",
            "jenkins",
            "python",
            "react",
            "angular",
            "spring ai",
            "claude",
            "openai",
            "rag",
            "vector database",
            "spark",
            "databricks",
            "snowflake",
            "etl",
            "grafana",
            "opentelemetry");

    @Override
    public ResumeAnalysisResponse analyze(
            String resumeText,
            String jobDescription) {

        String resume = resumeText.toLowerCase(Locale.ROOT);
        String jd = jobDescription.toLowerCase(Locale.ROOT);

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();

        for (String skill : TRACKED_SKILLS.stream().sorted().toList()) {

            if (jd.contains(skill)) {

                if (resume.contains(skill)) {
                    matched.add(display(skill));
                } else {
                    missing.add(display(skill));
                }
            }
        }

        int requiredCount = matched.size() + missing.size();

        int matchScore =
                requiredCount == 0
                        ? 70
                        : Math.min(
                                95,
                                55 + (matched.size() * 40 / requiredCount));

        int atsScore =
                Math.min(
                        96,
                        Math.max(
                                58,
                                matchScore - 3 + Math.min(matched.size(), 8)));

        int confidenceScore =
                requiredCount == 0
                        ? 72
                        : Math.min(
                                95,
                                70 + Math.min(requiredCount * 2, 25));

        String hiringRecommendation =
                recommendation(matchScore);

        List<String> criticalGaps = new ArrayList<>();
        List<String> preferredGaps = new ArrayList<>();

        classifyGaps(missing, criticalGaps, preferredGaps);

        List<String> transferableSkills =
                buildTransferableSkills(resume, jd);

        List<String> strengths = new ArrayList<>();

        if (!matched.isEmpty()) {
            strengths.add(
                    "Direct resume evidence exists for important job-description skills: "
                            + String.join(
                            ", ",
                            matched.stream().limit(7).toList())
                            + ".");
        }

        if (resume.contains("microservices")
                || resume.contains("distributed systems")) {

            strengths.add(
                    "Resume demonstrates enterprise architecture experience across "
                            + "microservices and distributed systems.");
        }

        if (resume.contains("aws")) {
            strengths.add(
                    "AWS experience provides strong alignment for cloud-native roles.");
        }

        if (resume.contains("terraform")) {
            strengths.add(
                    "Terraform experience demonstrates infrastructure-as-code capability.");
        }

        strengths.add(
                "Mock mode validates the V3 response structure without consuming Claude API credits.");

        List<String> weaknesses = new ArrayList<>();

        if (!criticalGaps.isEmpty()) {
            weaknesses.add(
                    "Critical gaps detected against the job description: "
                            + String.join(
                            ", ",
                            criticalGaps.stream().limit(5).toList())
                            + ".");
        }

        if (!preferredGaps.isEmpty()) {
            weaknesses.add(
                    "Preferred requirements not clearly supported by the resume: "
                            + String.join(
                            ", ",
                            preferredGaps.stream().limit(5).toList())
                            + ".");
        }

        weaknesses.add(
                "Mock mode uses keyword heuristics and cannot reason about semantic "
                        + "equivalence as deeply as Claude.");

        List<String> recommendations = new ArrayList<>();

        recommendations.add(
                "Move the strongest job-relevant accomplishments into the top third of the resume.");

        if (!matched.isEmpty()) {
            recommendations.add(
                    "Use supported JD terminology naturally where it accurately reflects "
                            + "real experience: "
                            + String.join(
                            ", ",
                            matched.stream().limit(5).toList())
                            + ".");
        }

        if (!criticalGaps.isEmpty()) {
            recommendations.add(
                    "Do not add critical missing technologies unless actual hands-on "
                            + "experience exists.");
        }

        if (!transferableSkills.isEmpty()) {
            recommendations.add(
                    "Explain transferable experience explicitly rather than presenting "
                            + "adjacent technologies as identical experience.");
        }

        List<String> resumeEvidence = new ArrayList<>();

        if (resume.contains("terraform")) {
            resumeEvidence.add(
                    "Terraform evidence detected in the resume for infrastructure-as-code work.");
        }

        if (resume.contains("sqs")
                || resume.contains("eventbridge")
                || resume.contains("event-driven")) {

            resumeEvidence.add(
                    "Event-driven architecture evidence detected through messaging or "
                            + "asynchronous workflow experience.");
        }

        if (resume.contains("redis")) {
            resumeEvidence.add(
                    "Redis evidence detected for caching and performance optimization.");
        }

        if (resume.contains("java")
                && resume.contains("spring boot")) {

            resumeEvidence.add(
                    "Java and Spring Boot implementation experience is directly supported.");
        }

        if (resume.contains("aws")) {
            resumeEvidence.add(
                    "AWS cloud experience is directly supported by the resume.");
        }

        if (resumeEvidence.isEmpty()) {
            resumeEvidence.add(
                    "Mock mode found limited structured evidence for the tracked skill set.");
        }

        String improvedSummary =
                "Mock mode does not generate a real rewritten summary. "
                        + "Claude mode will produce a role-specific summary based only on "
                        + "evidence found in the uploaded resume.";

        List<String> improvedBullets = List.of(
                "Claude mode will rewrite up to five high-value experience bullets using "
                        + "action, scope, technology, and outcome.",
                "Unsupported skills, metrics, and accomplishments will not be intentionally added.");

        List<String> interviewFocusAreas = new ArrayList<>();

        if (!matched.isEmpty()) {
            interviewFocusAreas.add(
                    "Prepare one concrete production example for each major matched skill.");
        }

        if (!criticalGaps.isEmpty()) {
            interviewFocusAreas.add(
                    "Prepare an honest explanation for critical gaps and a realistic ramp-up plan.");
        }

        if (!transferableSkills.isEmpty()) {
            interviewFocusAreas.add(
                    "Prepare to explain how transferable experience maps to the target role.");
        }

        interviewFocusAreas.add(
                "Prepare architecture, leadership, and production-support examples.");

        return new ResumeAnalysisResponse(
                matchScore,
                atsScore,
                confidenceScore,
                hiringRecommendation,
                "Mock V3 analysis completed successfully. "
                        + "Enable Claude mode for semantic analysis, evidence-based reasoning, "
                        + "gap classification, and tailored rewriting.",
                matched,
                missing,
                criticalGaps,
                preferredGaps,
                transferableSkills,
                strengths,
                weaknesses,
                recommendations,
                resumeEvidence,
                improvedSummary,
                improvedBullets,
                interviewFocusAreas,
                "MOCK");
    }

    private void classifyGaps(
            List<String> missing,
            List<String> criticalGaps,
            List<String> preferredGaps) {

        Set<String> highImpact =
                Set.of(
                        "Databricks",
                        "Spark",
                        "ETL",
                        "Kubernetes",
                        "EKS",
                        "Kafka");

        for (String skill : missing) {

            if (highImpact.contains(skill)) {
                criticalGaps.add(skill);
            } else {
                preferredGaps.add(skill);
            }
        }
    }

    private List<String> buildTransferableSkills(
            String resume,
            String jd) {

        List<String> transferable = new ArrayList<>();

        if ((jd.contains("connector")
                || jd.contains("integration"))
                && (resume.contains("rest")
                || resume.contains("fiserv")
                || resume.contains("twilio")
                || resume.contains("transmit security"))) {

            transferable.add(
                    "Third-party integration experience transferable to connector-platform work");
        }

        if ((jd.contains("stream")
                || jd.contains("real-time"))
                && (resume.contains("sqs")
                || resume.contains("eventbridge")
                || resume.contains("event-driven"))) {

            transferable.add(
                    "Event-driven processing experience transferable to real-time data workflows");
        }

        if (jd.contains("multi-cloud")
                && resume.contains("aws")) {

            transferable.add(
                    "Deep AWS cloud experience provides transferable cloud architecture fundamentals");
        }

        if (jd.contains("observability")
                && resume.contains("cloudwatch")) {

            transferable.add(
                    "CloudWatch monitoring experience transferable to broader observability tooling");
        }

        if (jd.contains("data pipeline")
                && (resume.contains("batch")
                || resume.contains("mapping")
                || resume.contains("event-driven"))) {

            transferable.add(
                    "Batch, mapping, and event-driven workflow experience transferable to data-pipeline concepts");
        }

        return transferable;
    }

    private String recommendation(int score) {

        if (score >= 90) {
            return "STRONG MATCH";
        }

        if (score >= 80) {
            return "GOOD MATCH";
        }

        if (score >= 65) {
            return "MODERATE MATCH";
        }

        return "WEAK MATCH";
    }

    private String display(String skill) {

        return switch (skill) {
            case "aws" -> "AWS";
            case "ecs" -> "ECS";
            case "eks" -> "EKS";
            case "sql" -> "SQL";
            case "rest" -> "REST APIs";
            case "rag" -> "RAG";
            case "oauth" -> "OAuth";
            case "etl" -> "ETL";
            case "opentelemetry" -> "OpenTelemetry";
            default ->
                    Character.toUpperCase(skill.charAt(0))
                            + skill.substring(1);
        };
    }
}