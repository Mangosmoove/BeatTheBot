package com.airesumescorer.controller;

import com.airesumescorer.model.Job;
import com.airesumescorer.model.Application;
import com.airesumescorer.dto.CheckDTO;
import com.airesumescorer.dto.ScoreRequestDTO;
import com.airesumescorer.dto.ScoreResultDTO;
import com.airesumescorer.repository.ApplicationRepository;
import com.airesumescorer.repository.JobRepository;
import com.airesumescorer.service.OpenAIService;
import com.airesumescorer.service.TikaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;

import java.util.Set;

@RestController
@RequestMapping("/api")
public class ApplicationController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private TikaService tikaService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Set<String> EXPECTED_CATEGORIES = Set.of("layout", "keywords", "sections", "formatting");

    @PostMapping(value = "/score", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Application> scoreApplication(
            @RequestPart("jobDescription") String jobDescription,
            @RequestPart("sessionToken") String sessionToken,
            @RequestPart("resume") MultipartFile resume) {

        String resumeText = tikaService.extractText(resume);

        Job job = new Job();
        job.setDescription(jobDescription);
        Job savedJob = jobRepository.save(job);

        Application app = new Application();
        app.setResumeText(resumeText);
        app.setSessionToken(sessionToken);
        app.setJob(savedJob);

        try {
            String aiRaw = openAIService.scoreResume(resumeText, jobDescription);
            ScoreResultDTO result = objectMapper.readValue(aiRaw, ScoreResultDTO.class);

            // check if a section is missing from result so user will not see missing response
            if (result.getSections() == null || !result.getSections().keySet().equals(EXPECTED_CATEGORIES)) {
                throw new IllegalStateException("AI response was missing one or more expected sections");
            }

            // recalculate parent passed based on children bc groq has issues w calculation
            result.getSections().forEach((categoryName, category) -> {
                boolean allPassed = category.getChecks().values()
                        .stream()
                        .allMatch(CheckDTO::isPassed);
                category.setPassed(allPassed);
            });

            app.setAiScore(result.getScore());
            app.setAiSections(objectMapper.writeValueAsString(result.getSections()));
        } catch (IllegalStateException e) {
            app.setAiScore(0);
            app.setAiSections("{\"error\": \"The scan came back incomplete. Please try again.\"}");
        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e){
            app.setAiScore(0);
            app.setAiSections("{\"error\": \"The scorer is busy right now. Please try again in about a minute.\"}");
        } catch (Exception e) {
            app.setAiScore(0);
            app.setAiSections("{\"error\": \"Scoring failed: " + e.getMessage() + "\"}");
        }

        Application savedApp = applicationRepository.save(app);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedApp);
    }
}
