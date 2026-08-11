# BeatTheBot — Client

A React front-end that helps job seekers "beat" Applicant Tracking Systems (ATS). Users upload their resume and paste a job description, and the app is designed to run an ATS-style scan that surfaces layout issues, keyword matches, section problems, and formatting red flags before they submit their application.

## Tech Stack

- **React 19** + **TypeScript**
- **Vite 8** — dev server & build tooling
- **Material UI (MUI) 9** (`@mui/material`, `@mui/icons-material`, `@emotion/react`, `@emotion/styled`) — form controls, buttons, theming
- **Tailwind CSS 4** (via `@tailwindcss/vite`) — utility-first layout & styling, using CSS custom properties for theming
- **lucide-react** — icon set
- **axios** — HTTP client (for future API calls)
- **ESLint** + **typescript-eslint** — linting

## Design System

The UI uses a "terminal / hacker" aesthetic:

- Deep black background with neon matrix-green accents (defined via `oklch()` CSS variables in `style.css`)
- `JetBrains Mono` monospace font throughout
- Custom Tailwind utilities: `text-glow`, `border-glow`, `scanline`, `cursor-blink`, `flicker`
- MUI theme (`theme.ts`) is kept in sync with the Tailwind CSS variables in `style.css` so MUI components (buttons, text fields) match the rest of the UI

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
