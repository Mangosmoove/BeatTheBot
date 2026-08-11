package com.airesumescorer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {
    @Value("${groq.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String scoreResume(String resumeText, String jobDescription) {
        String prompt = """
    You are a strict ATS (Applicant Tracking System) scanner. You do not give encouragement
    or coaching — you parse and score resumes the same way automated hiring software does.
    Address the candidate directly using "you" and "your" only when giving feedback in notes.
    
    Score the resume from 0-100 based on how well it would perform in a real ATS system.
    
    Evaluate the resume against these exact ATS criteria, organized by category:
    
    LAYOUT
    - single_column: Does the resume use a single-column layout? Multi-column layouts break ATS parsing.
    - no_images_icons_graphics_tables: Are there zero images, icons, graphics, or tables? These cause parsing failures.
    - no_hidden_text: Is there any hidden or white-on-white text used to game keyword matching?
    
    KEYWORDS
    - exact_keyword_matching: Do the exact keywords from the job description appear verbatim in the resume? Synonyms do not count.
    - keywords_in_context: Do keywords appear in Experience or Projects sections, not just listed in Skills?
    
    SECTIONS
    - standard_section_headers: Does the resume use standard ATS-recognized headers?
      (e.g. "Experience" or "Work Experience", "Education", "Skills", "Summary" or "Objective")
      Creative headers like "My Journey" or "What I've Built" will fail ATS parsing.
    - dedicated_skills_section: Is there a clearly labeled Skills section with relevant technical terms?
    
    FORMATTING
    - consistent_date_format: Are all dates in a consistent format throughout? (e.g. MM/YYYY or Month YYYY, not mixed)
    
    For each check:
    - Set "passed" to true or false
    - Write a specific "notes" value addressing the candidate directly ("you"/"your") explaining what passed or exactly what needs to change
    - Set the parent category "passed" to false if ANY child check within it fails
    
    Deduct points for each failed check. Be specific about what failed and why.
    
    Respond ONLY with valid JSON in this exact format, no extra text:
    {
      "score": <integer 0-100>,
      "sections": {
        "layout": {
          "passed": <boolean>,
          "checks": {
            "single_column":                   { "passed": <boolean>, "notes": "<feedback>" },
            "no_images_icons_graphics_tables": { "passed": <boolean>, "notes": "<feedback>" },
            "no_hidden_text":                  { "passed": <boolean>, "notes": "<feedback>" }
          }
        },
        "keywords": {
          "passed": <boolean>,
          "checks": {
            "exact_keyword_matching":          { "passed": <boolean>, "notes": "<feedback>" },
            "keywords_in_context":             { "passed": <boolean>, "notes": "<feedback>" }
          }
        },
        "sections": {
          "passed": <boolean>,
          "checks": {
            "standard_section_headers":        { "passed": <boolean>, "notes": "<feedback>" },
            "dedicated_skills_section":        { "passed": <boolean>, "notes": "<feedback>" }
          }
        },
        "formatting": {
          "passed": <boolean>,
          "checks": {
            "consistent_date_format":          { "passed": <boolean>, "notes": "<feedback>" }
          }
        }
      }
    }
    
    Job Description:
    %s
    
    Resume:
    %s
    """.formatted(jobDescription, resumeText);

        Map<String, Object> requestBody = Map.of(
                "model", "qwen/qwen3.6-27b",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                You are a resume scoring assistant.
                Return ONLY valid JSON.
                Do not output reasoning.
                Do not output <think> tags.
                Do not use markdown.
                """
                        ),
                        Map.of(
                                "role", "user",
                                "content", prompt
                        )
                ),
                "temperature", 0.3,
                "max_completion_tokens", 4096,
                "response_format", Map.of(
                        "type", "json_object"
                )
        );

        String response = restClient.post()
                .uri("https://api.groq.com/openai/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);

        System.out.println("=== GROQ RAW RESPONSE ===");
        System.out.println(response);
        System.out.println("=========================");
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Groq response", e);
        }
    }
}
