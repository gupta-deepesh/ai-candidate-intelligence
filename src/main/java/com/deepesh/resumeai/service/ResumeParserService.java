package com.deepesh.resumeai.service;

import com.deepesh.resumeai.exception.InvalidResumeException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeParserService {

    private static final int MAX_EXTRACTED_CHARACTERS = 60_000;

    public String extractText(MultipartFile file) {
        validate(file);

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document);

            if (text == null || text.isBlank()) {
                throw new InvalidResumeException(
                        "No readable text found. Image-only/scanned PDFs are not supported in V1.");
            }

            String cleaned = normalize(text);
            return cleaned.length() > MAX_EXTRACTED_CHARACTERS
                    ? cleaned.substring(0, MAX_EXTRACTED_CHARACTERS)
                    : cleaned;

        } catch (IOException ex) {
            throw new InvalidResumeException("Unable to read the uploaded PDF.", ex);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidResumeException("Please upload a resume PDF.");
        }

        String fileName = file.getOriginalFilename();
        boolean pdfName = fileName != null && fileName.toLowerCase().endsWith(".pdf");
        boolean pdfType = "application/pdf".equalsIgnoreCase(file.getContentType());

        if (!pdfName && !pdfType) {
            throw new InvalidResumeException("Only PDF resumes are supported in V1.");
        }
    }

    private String normalize(String value) {
        return value
                .replace("\u0000", "")
                .replaceAll("[\\t\\x0B\\f\\r]+", " ")
                .replaceAll(" +", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}
