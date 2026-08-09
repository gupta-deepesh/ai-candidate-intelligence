package com.deepesh.resumeai.service;

import com.deepesh.resumeai.dto.ResumeAnalysisResponse;
import com.deepesh.resumeai.exception.AiAnalysisException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.ai.mock", havingValue = "false")
public class ClaudeResumeAnalyzer implements ResumeAnalyzer {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public ClaudeResumeAnalyzer(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper) {

        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public ResumeAnalysisResponse analyze(
            String resumeText,
            String jobDescription) {

        String prompt = buildPrompt(
                resumeText,
                jobDescription);

        try {

            String content = callClaude(prompt);

            ResumeAnalysisResponse response =
                    parseResponse(content);

            return response.withAnalysisMode("CLAUDE");

        } catch (JsonProcessingException firstParseFailure) {

            /*
             * LLM output can occasionally be truncated or contain malformed JSON.
             * Make one controlled repair attempt before failing the request.
             */
            try {

                String originalContent =
                        callClaude(prompt);

                String repairedContent =
                        repairJson(originalContent);

                ResumeAnalysisResponse repaired =
                        parseResponse(repairedContent);

                return repaired.withAnalysisMode("CLAUDE");

            } catch (Exception retryFailure) {

                retryFailure.printStackTrace();

                throw new AiAnalysisException(
                        "Claude returned an invalid structured response. Please try again.",
                        retryFailure);
            }

        } catch (AiAnalysisException ex) {

            throw ex;

        } catch (Exception ex) {

            ex.printStackTrace();

            throw new AiAnalysisException(
                    "Claude analysis failed: " + ex.getMessage(),
                    ex);
        }
    }

    private String callClaude(String prompt) {

        String content =
                chatClient.prompt()
                        .system("""
                                You are an enterprise technical hiring evaluator.

                                You combine the perspective of:

                                - Principal Engineer hiring manager
                                - Senior technical recruiter
                                - ATS evaluator
                                - Enterprise software architect

                                Evaluate a candidate resume against a job description.

                                CRITICAL RULES:

                                1. The resume is the only source of truth about the candidate.

                                2. Never invent:
                                   - skills
                                   - technologies
                                   - employers
                                   - responsibilities
                                   - accomplishments
                                   - certifications
                                   - metrics
                                   - project experience

                                3. Distinguish direct experience from transferable experience.

                                4. Do not present adjacent technologies as identical experience.

                                5. Separate critical requirements from preferred requirements.

                                6. Be evidence-driven rather than keyword-driven.

                                7. Keep output concise and recruiter-friendly.

                                8. Return exactly one valid JSON object.

                                9. Do not use markdown.

                                10. Do not place explanatory text before or after the JSON.
                                """)
                        .user(prompt)
                        .call()
                        .content();

        if (content == null || content.isBlank()) {

            throw new AiAnalysisException(
                    "Claude returned an empty response.");
        }

        return content;
    }

    private ResumeAnalysisResponse parseResponse(
            String content)
            throws JsonProcessingException {

        String json =
                removeMarkdownFence(content);

        return objectMapper.readValue(
                json,
                ResumeAnalysisResponse.class);
    }

    private String repairJson(String brokenJson) {

        String repairPrompt = """
                The following text was intended to be a JSON response
                but may be malformed, truncated, or contain invalid quoting.

                Repair it into exactly one valid JSON object.

                IMPORTANT:

                - Do not add new candidate facts.
                - Do not invent resume experience.
                - Preserve the existing meaning.
                - Complete missing brackets or quotes where safely possible.
                - If an unfinished list item exists, shorten or remove that item.
                - Return JSON only.
                - No markdown.
                - No explanation.

                Required structure:

                {
                  "matchScore": 0,
                  "atsScore": 0,
                  "confidenceScore": 0,
                  "hiringRecommendation": "",
                  "executiveAssessment": "",
                  "matchedSkills": [],
                  "missingSkills": [],
                  "criticalGaps": [],
                  "preferredGaps": [],
                  "transferableSkills": [],
                  "strengths": [],
                  "weaknesses": [],
                  "atsRecommendations": [],
                  "resumeEvidence": [],
                  "improvedSummary": "",
                  "improvedBullets": [],
                  "interviewFocusAreas": [],
                  "analysisMode": "CLAUDE"
                }

                BROKEN JSON:

                %s
                """.formatted(brokenJson);

        String repaired =
                chatClient.prompt()
                        .system("""
                                You repair malformed JSON.

                                Return exactly one valid JSON object.
                                Never return markdown or commentary.
                                """)
                        .user(repairPrompt)
                        .call()
                        .content();

        if (repaired == null || repaired.isBlank()) {

            throw new AiAnalysisException(
                    "Claude JSON repair returned an empty response.");
        }

        return repaired;
    }

    private String buildPrompt(
            String resumeText,
            String jobDescription) {

        return """
                Analyze the RESUME against the JOB DESCRIPTION.

                Your goal is to determine whether this candidate should
                receive serious technical interview consideration.

                ==================================================
                RESPONSE LENGTH RULES
                ==================================================

                Keep the response concise.

                executiveAssessment:
                - Maximum 140 words.

                matchedSkills:
                - Maximum 15 items.
                - Maximum 15 words per item.

                missingSkills:
                - Maximum 8 items.
                - Maximum 18 words per item.

                criticalGaps:
                - Maximum 3 items.
                - Maximum 35 words per item.

                preferredGaps:
                - Maximum 5 items.
                - Maximum 30 words per item.

                transferableSkills:
                - Maximum 5 items.
                - Maximum 35 words per item.

                strengths:
                - Maximum 5 items.
                - Maximum 35 words per item.

                weaknesses:
                - Maximum 5 items.
                - Maximum 35 words per item.

                atsRecommendations:
                - Maximum 5 items.
                - Maximum 35 words per item.

                resumeEvidence:
                - Maximum 8 items.
                - Maximum 40 words per item.

                improvedSummary:
                - Maximum 100 words.

                improvedBullets:
                - Maximum 5 items.
                - Maximum 45 words per item.

                interviewFocusAreas:
                - Maximum 5 items.
                - Maximum 40 words per item.

                ==================================================
                SCORING
                ==================================================

                matchScore:
                0 to 100.

                Evaluate:
                - required skills
                - preferred skills
                - architecture depth
                - technical leadership
                - domain relevance
                - production ownership
                - transferable experience

                atsScore:
                0 to 100.

                Evaluate:
                - relevant terminology
                - keyword alignment
                - evidence
                - resume clarity
                - supported technology coverage

                Never reward unsupported keyword stuffing.

                confidenceScore:
                0 to 100.

                Represents how strongly resume evidence supports
                your conclusions.

                ==================================================
                HIRING RECOMMENDATION
                ==================================================

                Recommendation MUST follow these exact thresholds:

                matchScore 90-100:
                "STRONG MATCH"

                matchScore 80-89:
                "GOOD MATCH"

                matchScore 65-79:
                "MODERATE MATCH"

                matchScore 0-64:
                "WEAK MATCH"

                Do not deviate from these thresholds.

                ==================================================
                CRITICAL GAPS
                ==================================================

                criticalGaps must contain only requirements that
                materially affect the candidate's ability to perform
                the role.

                Do not make every missing technology critical.

                Examples:

                - mandatory platform expertise
                - central architecture requirement
                - major required domain knowledge
                - required leadership responsibility

                ==================================================
                PREFERRED GAPS
                ==================================================

                preferredGaps should contain:

                - preferred technologies
                - secondary technologies
                - learnable platform differences
                - requirements where strong transferable experience exists

                ==================================================
                TRANSFERABLE EXPERIENCE
                ==================================================

                Identify experience that legitimately reduces a gap.

                Example:

                Job requires Kafka.

                Resume contains:
                SQS + EventBridge + asynchronous event processing.

                Acceptable assessment:

                "SQS/EventBridge event-driven architecture provides
                transferable experience for Kafka-based messaging."

                Never say the candidate has Kafka production experience
                unless the resume demonstrates it.

                ==================================================
                RESUME EVIDENCE
                ==================================================

                Provide concrete evidence supporting important matches.

                Prefer concise evidence such as:

                "Java 17 / Spring Boot supported by implementation
                of core digital-banking microservices."

                Avoid repeating entire resume bullets unless necessary.

                ==================================================
                EXECUTIVE ASSESSMENT
                ==================================================

                Write this like a recruiter or hiring manager summary.

                Answer:

                - Why is the candidate worth interviewing?
                - What are the strongest differentiators?
                - What are the most important risks?

                Maximum 140 words.

                ==================================================
                ATS RECOMMENDATIONS
                ==================================================

                Only recommend resume changes supported by actual experience.

                Never recommend adding technologies merely because
                they appear in the job description.

                ==================================================
                IMPROVED SUMMARY
                ==================================================

                Rewrite the candidate's professional summary for this role.

                Rules:

                - Maximum 100 words.
                - Preserve only real experience.
                - Do not invent technologies.
                - Do not invent metrics.
                - Keep Principal/Senior engineering positioning.
                - Prioritize job-relevant capabilities.

                ==================================================
                IMPROVED EXPERIENCE BULLETS
                ==================================================

                Produce up to 5 bullets.

                Prefer:

                Action + Scope + Technology + Outcome

                Preserve real metrics and technologies.

                Never fabricate numbers.

                ==================================================
                INTERVIEW PREPARATION
                ==================================================

                Provide maximum 5 interview focus areas.

                Prioritize:

                - likely technical concerns
                - architecture stories
                - production examples
                - leadership examples
                - important gaps
                - transferable experience

                ==================================================
                OUTPUT
                ==================================================

                Return exactly one valid JSON object:

                {
                  "matchScore": 0,
                  "atsScore": 0,
                  "confidenceScore": 0,
                  "hiringRecommendation": "",
                  "executiveAssessment": "",
                  "matchedSkills": [],
                  "missingSkills": [],
                  "criticalGaps": [],
                  "preferredGaps": [],
                  "transferableSkills": [],
                  "strengths": [],
                  "weaknesses": [],
                  "atsRecommendations": [],
                  "resumeEvidence": [],
                  "improvedSummary": "",
                  "improvedBullets": [],
                  "interviewFocusAreas": [],
                  "analysisMode": "CLAUDE"
                }

                Return JSON only.

                ==================================================
                RESUME
                ==================================================

                %s

                ==================================================
                JOB DESCRIPTION
                ==================================================

                %s
                """.formatted(
                resumeText,
                jobDescription);
    }

    private String removeMarkdownFence(
            String value) {

        String cleaned =
                value.trim();

        if (cleaned.startsWith("```")) {

            cleaned =
                    cleaned.replaceFirst(
                            "^```(?:json)?\\s*",
                            "");

            cleaned =
                    cleaned.replaceFirst(
                            "\\s*```$",
                            "");
        }

        return cleaned.trim();
    }
}