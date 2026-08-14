package com.deepesh.resumeai.service;

import com.deepesh.resumeai.dto.ResumeAnalysisResponse;
import com.deepesh.resumeai.exception.InvalidResumeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ResumeAnalysisServiceTest {

    @Mock
    private ResumeParserService resumeParserService;

    @Mock
    private ResumeAnalyzer resumeAnalyzer;

    @Captor
    private ArgumentCaptor<String> resumeTextCaptor;

    @Captor
    private ArgumentCaptor<String> jobDescCaptor;

    @InjectMocks
    private ResumeAnalysisService resumeAnalysisService;

    @Test
    void analyze_shouldThrowInvalidResumeException_whenJobDescriptionIsNull() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);

        assertThatThrownBy(() -> resumeAnalysisService.analyze(file, null))
                .isInstanceOf(InvalidResumeException.class)
                .hasMessageContaining("Job description is required");
    }

    @Test
    void analyze_shouldThrowInvalidResumeException_whenJobDescriptionIsBlank() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);

        assertThatThrownBy(() -> resumeAnalysisService.analyze(file, "   "))
                .isInstanceOf(InvalidResumeException.class)
                .hasMessageContaining("Job description is required");
    }

    @Test
    void analyze_shouldReturnResponse_whenValidInputs() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);

        given(resumeParserService.extractText(file)).willReturn("extracted resume text");

        ResumeAnalysisResponse expected = new ResumeAnalysisResponse(
                80,
                75,
                90,
                "GOOD MATCH",
                "Strong candidate",
                List.of("Java", "Spring"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of("Good leadership"),
                List.of(),
                List.of(),
                List.of(),
                "Improved summary",
                List.of(),
                List.of(),
                "MOCK"
        );

        given(resumeAnalyzer.analyze("extracted resume text", "some JD")).willReturn(expected);

        ResumeAnalysisResponse actual = resumeAnalysisService.analyze(file, "some JD");

        assertThat(actual).isNotNull();
        assertThat(actual.matchScore()).isEqualTo(80);
        assertThat(actual.hiringRecommendation()).isEqualTo("GOOD MATCH");

        verify(resumeParserService).extractText(file);
        verify(resumeAnalyzer).analyze(resumeTextCaptor.capture(), jobDescCaptor.capture());

        assertThat(resumeTextCaptor.getValue()).isEqualTo("extracted resume text");
        assertThat(jobDescCaptor.getValue()).isEqualTo("some JD");
    }

    @Test
    void analyze_shouldTruncateLongJobDescription_beforeCallingAnalyzer() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);

        String resumeText = "resume text here";
        given(resumeParserService.extractText(file)).willReturn(resumeText);

        // create a job description longer than the hardcoded limit in service (25_000)
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 26000; i++) {
            sb.append('a');
        }
        String longJobDesc = sb.toString();

        ResumeAnalysisResponse dummy = new ResumeAnalysisResponse(
                0,0,0,"","",List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),List.of(),"",List.of(),List.of(),"MOCK"
        );

        given(resumeAnalyzer.analyze(any(String.class), any(String.class))).willReturn(dummy);

        resumeAnalysisService.analyze(file, longJobDesc);

        verify(resumeAnalyzer).analyze(resumeTextCaptor.capture(), jobDescCaptor.capture());

        String passedJobDesc = jobDescCaptor.getValue();

        // service truncates to 25_000 characters
        assertThat(passedJobDesc.length()).isEqualTo(25_000);
    }

    @Test
    void analyze_shouldPropagateParserExceptions() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);

        given(resumeParserService.extractText(file)).willThrow(new InvalidResumeException("bad resume"));

        assertThatThrownBy(() -> resumeAnalysisService.analyze(file, "valid JD"))
                .isInstanceOf(InvalidResumeException.class)
                .hasMessageContaining("bad resume");
    }
}
