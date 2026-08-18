# BeatTheBot

BeatTheBot helps job seekers see their resume the way an Applicant Tracking System (ATS) does. Users upload a resume and paste a job description; the app scans for layout issues, keyword mismatches, missing sections, and formatting problems that could get a resume auto-rejected before a human ever reads it.

You can interact with a deployed version of BeatTheBot at [https://beat-the-bot-gray.vercel.app](https://beat-the-bot-gray.vercel.app/). Please note that if it takes a while to receive your score, it's because I used the free tier of Render, which can take some time to spin up if it is in an idle state.

The project is split into two independently deployable pieces:

```
├── client/   # React + TypeScript frontend (Vite)
└── server/   # Spring Boot backend (Java)
```

See [`client/README.md`](./client/README.md) and [`server/README.md`](./server/README.md) for stack details, project structure, and local setup specific to each.

## Architecture

```
┌─────────────┐        multipart/form-data         ┌──────────────┐        chat completion        ┌────────────┐
│   Client    │ ─────────────────────────────────▶ │    Server    │ ────────────────────────────▶ │  Groq API  │
│ (React/Vite)│ ◀───────────────────────────────── │ (Spring Boot)│ ◀──────────────────────────── │ (LLM score)│
└─────────────┘         Application JSON           └──────┬───────┘                               └────────────┘
                                                          │
                                                          ▼
                                                   ┌──────────────┐
                                                   │  PostgreSQL  │
                                                   └──────────────┘
```

1. The client uploads a resume file + job description to the server's `POST /api/score` endpoint.
2. The server extracts resume text (Apache Tika), sends it with the job description to Groq's LLM API for ATS-style scoring, and re-derives category pass/fail from the individual checks.
3. The result is persisted to Postgres and returned to the client, which renders a score breakdown by category.

## Configuration

Each side of the app reads its environment-specific config from environment variables rather than hardcoded values, so the same code deploys anywhere without modification.

**Client** (`VITE_API_URL`) — the backend URL the frontend calls.

**Server** (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JPA_DDL_AUTO`, `GROQ_API_KEY`, `CORS_ALLOWED_ORIGINS`) — see [`server/README.md`](./server/README.md#configuration) for the full table and local defaults.

## Local Development

1. Start Postgres locally and create an `airesumescorer` database.
2. In `server/`, set a `GROQ_API_KEY` (via env var or `application-local.properties`) and run `./mvnw spring-boot:run`. The server listens on `:8080`.
3. In `client/`, set `VITE_API_URL=http://localhost:8080` in `.env.development` and run `npm run dev`. The client listens on `:5173`.

## Deployment

- **Client**: deployed on [Vercel](https://vercel.com), built from the `client/` directory (root directory set to `client`, Vite preset auto-detected).
- **Server**: deployed on [Render](https://render.com) as a Docker-based web service (root directory set to `server/airesumescorer`, where the `Dockerfile`, `pom.xml`, and `mvnw` live). Render's native Java buildpack doesn't yet support the Java version this project targets, so the server ships as a multi-stage Docker build (`eclipse-temurin:25-jdk` to compile, `eclipse-temurin:25-jre` to run).
- **Database**: [Neon](https://neon.tech) managed Postgres (free tier).

After deploying, the two are wired together via environment variables:
- Server's `CORS_ALLOWED_ORIGINS` (Render) is set to the client's deployed Vercel URL, e.g. `https://your-project.vercel.app` (no trailing slash).
- Client's `VITE_API_URL` (Vercel) is set to the server's deployed Render URL, e.g. `https://your-service.onrender.com` (no trailing slash).

Neon's single connection string needs to be split into the three vars the server expects:

```
postgresql://[USERNAME]:[PASSWORD]@[HOST]/[DBNAME]?sslmode=require
```

| Variable | Value |
|---|---|
| `DATABASE_USERNAME` | `[USERNAME]` |
| `DATABASE_PASSWORD` | `[PASSWORD]` |
| `DATABASE_URL` | `jdbc:postgresql://[HOST]/[DBNAME]?sslmode=require` |

Note the `jdbc:` prefix gets added and the `username:password@` portion is stripped out into the separate vars above.

**Cold starts**: Render's free tier spins the service down after ~15 min idle; the next request can take 30–60s to wake it back up. Expected behavior on the free tier, not a bug.

## Future Enhancements
- **Resubmission feature** — allow a user to resubmit a scan (e.g. after a scoring failure or to rescan an updated resume) using `ApplicationRepository`'s existing `findTop2ByJobIdAndSessionTokenOrderBySubmittedAtDesc` lookup as a starting point.
- **Event logging** — add structured server-side logging/monitoring around scoring failures (currently persisted as `aiScore = 0` with an error message in `aiSections` rather than surfaced as an HTTP error) so failures are visible to operators without changing the client-facing response contract.