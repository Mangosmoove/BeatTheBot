# BeatTheBot — Client

A React front-end that helps job seekers "beat" Applicant Tracking Systems (ATS). Users upload their resume and paste a job description, and the app runs an ATS-style scan that surfaces layout issues, keyword matches, section problems, and formatting red flags before they submit their application.

## Tech Stack

- **React 19** + **TypeScript**
- **Vite 8**
- **Material UI (MUI) 9** (`@mui/material`, `@mui/icons-material`, `@emotion/react`, `@emotion/styled`)
- **Tailwind CSS 4** (via `@tailwindcss/vite`)
- **lucide-react**
- **axios**
- **ESLint** + **typescript-eslint**

## Design System

The UI uses a "terminal / hacker" aesthetic:

- Deep black background with neon matrix-green accents (defined via `oklch()` CSS variables in `style.css`)
- `JetBrains Mono` monospace font throughout
- Custom Tailwind utilities: `text-glow`, `border-glow`, `scanline`, `cursor-blink`, `flicker`
- MUI theme (`theme.ts`) is kept in sync with the Tailwind CSS variables in `style.css` so MUI components (buttons, text fields) match the rest of the UI

## Configuration

The client talks to the backend at a URL controlled by the `VITE_API_URL` environment variable, read via `import.meta.env.VITE_API_URL`.

Create a `.env.development` for local dev:
`VITE_API_URL=http://localhost:8080`

In production (e.g. Vercel), set `VITE_API_URL` to your deployed backend's URL as a platform environment variable rather than committing a `.env.production` file.

## Getting Started

### Prerequisites

- Node.js (a recent LTS version is recommended)
- npm

### Installation

```bash
npm install
```

### Development

Start the Vite dev server with hot module reloading:

```bash
npm run dev
```

### Build

Type-check and build for production:

```bash
npm run build
```

### Preview

Preview the production build locally:

```bash
npm run preview
```

### Lint

```bash
npx prettier . --write
```

## Deployment

Deployed on Vercel, built from this repo. Set `VITE_API_URL` in the project's environment variables to point at the live backend before deploying.