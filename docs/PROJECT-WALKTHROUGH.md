# Project Walkthrough

## Request flow

1. Browser posts PDF + job description as multipart form data.
2. `ResumeAnalysisController` receives the request.
3. `ResumeAnalysisService` orchestrates the use case.
4. `ResumeParserService` extracts PDF text using PDFBox.
5. `ResumeAnalyzer` is the strategy abstraction.
6. Mock mode uses deterministic keyword matching.
7. Claude mode uses Spring AI `ChatClient`.
8. Claude returns structured JSON.
9. Jackson maps JSON into `ResumeAnalysisResponse`.
10. Browser renders the result.

## Interview concepts

- Spring AI abstraction
- Prompt engineering
- System prompts
- Temperature
- JSON structured output
- Hallucination controls
- Context/token limits
- PDF text extraction
- Layered architecture
- Strategy pattern
- Input validation
- Global exception handling
- API cost and latency
- Secret management
- RAG and vector databases

## Why mock mode?

It lets development and testing run without paying for LLM calls.

## Why a ResumeAnalyzer interface?

The model provider can be replaced without changing the controller or orchestration layer.

Implementations can later include OpenAI, Gemini, Bedrock, or Ollama.

## Production security additions

- Malware scanning
- Strong file-content validation
- Authentication/authorization
- PII-safe logging
- Encrypt storage
- AWS Secrets Manager
- Rate limiting
- WAF
- Avoid persisting resumes unless explicitly requested
- Prompt-injection defenses
