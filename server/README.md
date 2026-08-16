# AI Resume Scorer — Server

A Spring Boot backend that scores resumes against a job description the way an Applicant Tracking System (ATS) would. It extracts text from an uploaded resume file, sends it (along with the job description) to an LLM for scoring, and persists the results.

## Tech Stack

- **Java** + **Spring Boot**
  - `spring-boot-starter-web` — REST controllers
  - `spring-boot-starter-data-jpa` — persistence via Spring Data JPA
- **Jakarta Persistence (JPA)** — entity mapping
- **Apache Tika** — extracts raw text from uploaded resume files (PDF, DOCX, etc.)
- **Groq API** (OpenAI-compatible chat completions endpoint, `qwen/qwen3.6-27b`) — LLM-based ATS scoring, called via Spring's `RestClient`
- **Jackson** (`tools.jackson`) — JSON (de)serialization
- A relational database via JPA (configure your own datasource, e.g. Postgres/MySQL/H2)

## Project Structure

```
src/main/java/com/airesumescorer/
├── AIResumeScorer.java              # Spring Boot application entry point
├── controller/
│   └── ApplicationController.java   # REST endpoints (POST /api/score)
├── dto/
│   ├── ScoreRequestDTO.java         # Incoming request shape (jobDescription, sessionToken)
│   ├── ScoreResultDTO.java          # Parsed LLM response: overall score + sections
│   ├── CategoryDTO.java             # A scoring category (layout, keywords, sections, formatting)
│   └── CheckDTO.java                # An individual pass/fail check with notes
├── model/
│   ├── Application.java             # JPA entity: a submitted resume + its AI score/sections
│   └── Job.java                     # JPA entity: a job description
├── repository/
│   ├── ApplicationRepository.java   # Spring Data repository for Application
│   └── JobRepository.java           # Spring Data repository for Job
└── service/
    ├── OpenAIService.java           # Builds the ATS-scoring prompt and calls the Groq LLM API
    └── TikaService.java             # Extracts plain text from uploaded resume files
```

## How It Works

1. Client sends a `multipart/form-data` request to `POST /api/score` with:
   - `resume` — the resume file (PDF/DOCX/etc.)
   - `jobDescription` — the job description text
   - `sessionToken` — a client-generated session identifier
2. `TikaService` extracts plain text from the uploaded resume.
3. A `Job` row is created/saved with the job description.
4. `OpenAIService` sends a structured prompt (resume text + job description) to the Groq chat completions API, asking for a strict JSON response containing:
   - An overall `score` (0–100)
   - `sections` broken into `layout`, `keywords`, `sections`, and `formatting`, each with individual pass/fail `checks` and feedback notes
5. The controller re-derives each category's `passed` flag from its child checks (rather than trusting the LLM's own aggregation), since the LLM isn't always reliable at that arithmetic.
6. The resulting `Application` (resume text, AI score, AI section breakdown, job reference, session token, timestamp) is persisted and returned to the client with `201 Created`.
7. If scoring fails for any reason, the application is still saved with `aiScore = 0` and an error message in `aiSections`, rather than failing the whole request.

## API

### `POST /api/score`

**Content-Type:** `multipart/form-data`

| Part | Type | Description |
|---|---|---|
| `resume` | file | Resume file to be parsed (PDF, DOCX, etc.) |
| `jobDescription` | string | Job description text to score the resume against |
| `sessionToken` | string | Client-provided session identifier |

**Response:** `201 Created` with the saved `Application` object, including:
- `id`
- `job` (the associated `Job`)
- `sessionToken`
- `resumeText`
- `aiScore`
- `aiSections` (JSON string of category/check results)
- `submittedAt`

## Data Model

- **Job** — `id`, `description`
- **Application** — `id`, `job` (many-to-one), `sessionToken`, `resumeText`, `aiScore`, `aiSections`, `submittedAt` (auto-set on create)

`ApplicationRepository` also exposes `findTop2ByJobIdAndSessionTokenOrderBySubmittedAtDesc(jobId, sessionToken)` for fetching a session's most recent submissions for a given job.

## Configuration

The service requires a Groq API key, supplied via the `groq.api.key` property (e.g. in `application.properties`/`application.yml`, or as an environment variable depending on your Spring configuration setup):

```properties
groq.api.key=your-groq-api-key
```

You'll also need to configure a datasource for JPA (e.g. `spring.datasource.*` properties) since no datasource configuration is included in the files shown here.

## Running Locally

```bash
./mvnw spring-boot:run
```

or, if using Gradle:

```bash
./gradlew bootRun
```

## Notes / Known Considerations
- No authentication/authorization is present on the `/api/score` endpoint; `sessionToken` is client-supplied and not validated server-side.
- Error handling in `ApplicationController` currently swallows scoring exceptions into the saved record rather than returning an error HTTP status.
