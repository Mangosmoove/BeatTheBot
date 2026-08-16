# BeatTheBot

BeatTheBot helps job seekers see their resume the way an Applicant Tracking System (ATS) does. Users upload a resume and paste a job description; the app scans for layout issues, keyword mismatches, missing sections, and formatting problems that could get a resume auto-rejected before a human ever reads it.

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

- **Server + database**: deployed on Railway (Spring Boot service + managed Postgres instance in the same project).
- **Client**: deployed on Vercel, built from the `client/` directory.

After deploying, the two are wired together via environment variables:
- Server's `CORS_ALLOWED_ORIGINS` is set to the client's deployed URL.
- Client's `VITE_API_URL` is set to the server's deployed URL.

## Known Limitations

- No authentication/authorization is present on the `/api/score` endpoint; `sessionToken` is client-supplied and not validated server-side.

## Future Enhancements

- **Resubmission feature** — allow a user to resubmit a scan (e.g. after a scoring failure or to rescan an updated resume) using `ApplicationRepository`'s existing `findTop2ByJobIdAndSessionTokenOrderBySubmittedAtDesc` lookup as a starting point.
- **Event logging** — add structured server-side logging/monitoring around scoring failures (currently persisted as `aiScore = 0` with an error message in `aiSections` rather than surfaced as an HTTP error) so failures are visible to operators without changing the client-facing response contract.