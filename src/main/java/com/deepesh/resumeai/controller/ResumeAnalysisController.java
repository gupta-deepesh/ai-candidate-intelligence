package com.deepesh.resumeai.controller;

import com.deepesh.resumeai.dto.ResumeAnalysisResponse;
import com.deepesh.resumeai.service.ResumeAnalysisService;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
@Validated
public class ResumeAnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;

    public ResumeAnalysisController(ResumeAnalysisService resumeAnalysisService) {
        this.resumeAnalysisService = resumeAnalysisService;
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResumeAnalysisResponse analyze(@RequestPart("resume") MultipartFile resume,
            @RequestParam("jobDescription") @NotBlank(message = "Job description is required") String jobDescription) {

        return resumeAnalysisService.analyze(resume, jobDescription);
    }
}
