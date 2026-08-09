# AI Candidate Intelligence Platform

> An AI-powered candidate intelligence platform built with **Java 17**, **Spring Boot**, **Spring AI**, and **Anthropic Claude**.

The application evaluates a candidate's resume against a job description and produces evidence-based hiring insights, ATS alignment, skill-gap analysis, transferable experience, resume recommendations, and interview preparation guidance.

---

## Overview

Traditional resume scanners often focus heavily on keyword matching.

This project goes further by using a Large Language Model to evaluate:

- how well a candidate fits a specific role
- which skills are directly demonstrated
- which important requirements are missing
- which existing skills are transferable
- what resume evidence supports the evaluation
- whether the candidate is worth interviewing
- how the resume can be improved for the role
- what areas the candidate should prepare for during interviews

The goal of the project is to demonstrate practical Generative AI integration in a production-style Java application rather than simply calling an LLM API and displaying raw text.

---

## Key Features

### Candidate Intelligence

- Job Match Score
- ATS Score
- Analysis Confidence Score
- Hiring Recommendation
- Executive Assessment
- Interview Readiness

### Skill Analysis

- Matched Skills
- Missing Skills
- Critical Skill Gaps
- Preferred Skill Gaps
- Transferable Skills

### Explainable AI

The application attempts to ground its recommendations in resume evidence rather than relying only on keyword matching.

It provides:

- Resume Evidence
- Strengths
- Weaknesses
- Evidence-based candidate assessment
- Transferable experience mapping

### Resume Recommendations

- ATS Recommendations
- Improved Professional Summary
- Improved Experience Bullets
- Copy-to-clipboard support

### Interview Preparation

- Technical focus areas
- Architecture discussion areas
- Leadership discussion points
- Important gaps to prepare for
- Transferable-experience discussion guidance

### User Experience

- PDF Resume Upload
- Drag-and-Drop Upload
- Job Description Input
- Live Claude Analysis
- Demo Mode
- Live Analysis Timer
- Progressive Analysis Status
- Recruiter Snapshot
- Responsive Dashboard
- JSON Export
- Friendly AI Error Handling

---

## Recruiter Snapshot

The report includes a concise recruiter-focused summary showing:

- Interview Readiness
- Top Strengths
- Top Risks
- Hiring Recommendation

This provides a quick view before reviewing the detailed analysis.

---

## Demo Mode

The application includes a sample demo mode that allows visitors to experience the product without uploading a resume or consuming Claude API credits.

Demo Mode simulates the analysis workflow:

1. Reading resume and job description
2. Understanding role requirements
3. Matching candidate evidence
4. Generating hiring recommendations
5. Preparing the Candidate Intelligence Report

---

## Technology Stack

### Backend

- Java 17
- Spring Boot
- Spring AI
- Anthropic Claude API
- Apache PDFBox
- Jackson
- Gradle

### Frontend

- HTML5
- CSS3
- Vanilla JavaScript

### AI Engineering

- Prompt Engineering
- Structured JSON Output
- Evidence-Based Evaluation
- Explainable AI
- LLM Response Validation
- JSON Repair / Retry Handling
- Resume-to-Job Matching

---

## Architecture

```text
+----------------------+
|     PDF Resume       |
+----------+-----------+
           |
           v
+----------------------+
|   Apache PDFBox      |
|   Text Extraction    |
+----------+-----------+
           |
           v
+----------------------+
|   Prompt Builder     |
| Resume + Job Desc.   |
+----------+-----------+
           |
           v
+----------------------+
|  Anthropic Claude    |
|     via Spring AI    |
+----------+-----------+
           |
           v
+----------------------+
| Structured JSON      |
| Validation / Repair  |
+----------+-----------+
           |
           v
+----------------------+
|   Spring Boot API    |
+----------+-----------+
           |
           v
+----------------------+
| Candidate Intelligence|
|      Dashboard       |
+----------------------+
```

---

## AI Evaluation Flow

The live analysis process follows this general workflow:

```text
Resume PDF
   +
Job Description
        |
        v
Resume Text Extraction
        |
        v
Role Requirement Analysis
        |
        v
Candidate Evidence Matching
        |
        v
Skill & Gap Evaluation
        |
        v
Hiring Recommendation
        |
        v
Resume Recommendations
        |
        v
Interview Preparation
        |
        v
Candidate Intelligence Report
```

---

## Example Output

A completed analysis can include:

```text
Job Match               84%
ATS Score               78%
Analysis Confidence     92%

Hiring Recommendation
GOOD MATCH
```

Followed by:

- Executive Assessment
- Matched Skills
- Missing Skills
- Critical Gaps
- Preferred Gaps
- Transferable Skills
- Strengths
- Weaknesses
- Resume Evidence
- ATS Recommendations
- Improved Summary
- Improved Experience Bullets
- Interview Focus Areas

---

## Scoring

The user interface uses consistent recommendation thresholds:

```text
90 - 100   STRONG MATCH
80 - 89    GOOD MATCH
65 - 79    MODERATE MATCH
0  - 64    WEAK MATCH
```

The numeric score and hiring recommendation are kept aligned in the UI.

---

## LLM Reliability

LLM responses are not always deterministic.

The application includes handling for cases such as:

- malformed JSON
- incomplete structured responses
- empty AI responses
- API failures
- authentication failures
- rate limits

The backend requests structured JSON and can attempt controlled response repair before returning an error to the user.

---

## Privacy

Resume files are processed for analysis and are not intentionally persisted by the application.

For a public deployment, users should avoid uploading confidential or sensitive information they do not wish to send to an external AI provider.

---

## Running Locally

### Prerequisites

You need:

```text
Java 17
Gradle
Anthropic API Key
```

Verify Java:

```bash
java -version
```

Verify Gradle:

```bash
gradle -version
```

---

### Clone Repository

```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/ai-candidate-intelligence.git
```

Then:

```bash
cd ai-candidate-intelligence
```

---

## Configure Anthropic API Key

### Windows PowerShell

```powershell
$env:ANTHROPIC_API_KEY="your-api-key"
```

### Windows Command Prompt

```cmd
set ANTHROPIC_API_KEY=your-api-key
```

### macOS / Linux

```bash
export ANTHROPIC_API_KEY=your-api-key
```

Never commit your Anthropic API key to source control.

---

## Start the Application

```bash
gradle bootRun
```

Open:

```text
http://localhost:8080
```

---

## Environment Variables

| Variable | Description |
|---|---|
| `ANTHROPIC_API_KEY` | Anthropic API key used for live Claude analysis |

---

## Project Structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/deepesh/resumeai/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── exception/
│   │       └── service/
│   │
│   └── resources/
│       ├── static/
│       │   ├── index.html
│       │   ├── styles.css
│       │   └── app.js
│       │
│       └── application.yml
│
├── test/
│
build.gradle
settings.gradle
README.md
```

---

## Screenshots

### Landing Page

Add screenshot after GitHub upload.

### Live Analysis

Add screenshot showing:

```text
Claude AI Live Analysis
Elapsed: 00:27

Reading resume and job description        ✓
Understanding role requirements           ✓
Matching candidate evidence               ✓
Generating hiring recommendations
```

### Candidate Intelligence Report

Add screenshot showing:

- Recruiter Snapshot
- Job Match
- ATS Score
- Analysis Confidence
- Hiring Recommendation

---

## Live Demo

Public deployment coming soon.

After deployment, replace this section with:

```text
Live Demo:
https://YOUR-LIVE-APP-URL
```

---

## Design Goals

This project was designed around several engineering principles:

- keep the resume as the primary source of candidate truth
- avoid inventing unsupported candidate experience
- distinguish direct experience from transferable experience
- separate critical gaps from preferred gaps
- generate recruiter-readable output
- return structured AI responses
- gracefully handle malformed LLM output
- provide a usable experience during longer AI response times

---

## Why I Built This

I built this project to gain hands-on experience integrating Generative AI into an enterprise-style Java application.

The objective was not simply to create another resume keyword scanner.

I wanted to explore how an LLM can be used as part of an application workflow involving:

- Java backend services
- Spring AI
- prompt engineering
- structured output
- response validation
- explainable AI
- error recovery
- production-style UX

---

## What I Learned

This project provided hands-on experience with:

- Spring AI integration
- Anthropic Claude API usage
- prompt design and refinement
- controlling LLM output structure
- JSON parsing and recovery
- handling nondeterministic AI responses
- building AI-backed REST workflows
- PDF text extraction
- asynchronous-feeling UI experiences for long-running AI calls
- balancing AI recommendations with evidence

---

## Roadmap

Potential future enhancements include:

- PDF report export
- persistent analysis history
- authenticated users
- API rate limiting
- improved observability
- RAG-based interview preparation
- vector database integration
- embeddings
- AI interview coach
- Model Context Protocol (MCP)
- tool calling
- agent-based workflows

These are intentionally future items and are not required for the current release.

---

## Author

**Deepesh Gupta**

Principal Engineer

Java | Spring Boot | AWS | Generative AI

---

## Disclaimer

This application provides AI-generated candidate analysis and recommendations.

The results should be treated as decision-support information rather than an automated hiring decision.

LLM-generated output may occasionally be incomplete or inaccurate and should be reviewed by a human.

---

## License
MIT License