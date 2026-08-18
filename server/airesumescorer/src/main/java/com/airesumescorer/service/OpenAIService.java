package com.airesumescorer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {
    @Value("${groq.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String scoreResume(String resumeText, String jobDescription) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

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
      NOTE: This text was extracted from a PDF using an automated tool. PDFs often embed a bookmark
      or outline structure separate from the visible page content, which gets extracted as a short
      list of bare section header words with no accompanying content, usually appearing at the very
      end of the text (e.g. a trailing "EXPERIENCE PROJECTS SKILLS EDUCATION" with nothing under each).
      This is an extraction artifact, not real duplicate content in the visible resume — ignore any
      such trailing bare-header list entirely and do not treat it as duplicate section headers.
    - dedicated_skills_section: Is there a clearly labeled Skills section with relevant technical terms?
    
    FORMATTING
    - consistent_date_format: Are all dates in a consistent format throughout? (e.g. MM/YYYY or Month YYYY, not mixed)
      "Present" or "Current" as an end date for an ongoing role is standard and NOT a formatting
      inconsistency — do not flag it, and do not flag a start date paired with "Present" as suspicious
      just because it is recent. Today's real date is %s. Before flagging any date as a "future date"
      issue, compare it carefully and step by step: first compare the year, and only if the years are
      equal, compare the month number (January=1 through December=12) within that year. A date is only
      "in the future" if it is strictly later than today's date by this comparison — do not flag a date
      as future just because it is recent, close to today, or in the same year as today. If in doubt,
      do NOT flag it as a future date.
    
    For each check:
    - Set "passed" to true or false
    - Write a specific "notes" value addressing the candidate directly ("you"/"your") explaining what passed or exactly what needs to change
    - Set the parent category "passed" to false if ANY child check within it fails
    
    Deduct points for each failed check. Be specific about what failed and why.
    
    IMPROVEMENTS
    After evaluating all checks, write a prioritized list of the 3-5 highest-impact improvements
    the candidate should make, ordered from most to least impactful. Prioritize failed checks in
    layout first (these can break parsing entirely), then keywords, then sections, then formatting.
    If fewer than 3 checks failed, include fewer improvements — never pad the list with restated
    passing checks or generic advice. Each improvement should be one direct, actionable sentence
    addressed to the candidate ("you"/"your"), specific enough to act on without repeating a check's
    "notes" verbatim.
    
    Respond ONLY with valid JSON in this exact format, no extra text:
    {
      "score": <integer 0-100>,
      "improvements": [ "<top improvement>", "<next improvement>", "..." ],
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
    """.formatted(today, jobDescription, resumeText);

        Map<String, Object> requestBody = Map.of(
                "model", "qwen/qwen3.6-27b",
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                You are a resume scoring assistant.
                Return ONLY valid JSON.
                Do not output reasoning, self-correction, or internal deliberation anywhere, including inside "notes" fields.
                Each "notes" value must be a single direct sentence of feedback only — no meta-commentary about your own scoring process.
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
                "max_completion_tokens", 2000,
                "reasoning_effort", "none",
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
