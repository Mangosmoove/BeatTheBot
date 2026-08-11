import Features from './Features';
import Hero from './Hero';
import ResumeUpload from './ResumeUpload';
import JobDescriptionInput from './JobDescriptionInput';
import { useState, type SubmitEvent } from 'react';
import { Button, Alert, CircularProgress } from '@mui/material';
import type { Application } from '../types/Application';
import { getSessionToken } from '../utils/session';

interface LandingViewProps {
  onResult: (app: Application) => void;
}

const LandingView = ({ onResult }: LandingViewProps) => {
  const [file, setFile] = useState<File | null>(null);
  const [jd, setJd] = useState('');
  const [status, setStatus] = useState<'idle' | 'loading'>('idle');
  const [error, setError] = useState<string | null>(null);

  const onSubmit = async (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!file || jd.trim().length < 20) return;

    setError(null);
    setStatus('loading');

    const formData = new FormData();
    formData.append('resume', file);
    formData.append('jobDescription', jd);
    formData.append('sessionToken', getSessionToken());

    try {
      const res = await fetch('http://localhost:8080/api/score', {
        method: 'POST',
        body: formData,
      });

      if (!res.ok) throw new Error(`Server returned ${res.status}`);

      const data: Application = await res.json();
      onResult(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Something went wrong');
    } finally {
      setStatus('idle');
    }
  };

  return (
    <>
      <Hero />
      <form onSubmit={onSubmit} className="mt-12 grid gap-6 md:grid-cols-2">
        <ResumeUpload file={file} onFileChange={setFile} />
        <JobDescriptionInput value={jd} onChange={setJd} />

        <div className="md:col-span-2 space-y-4">
          {error && (
            <Alert severity="error" variant="outlined">
              {error}
            </Alert>
          )}
          <Button
            type="submit"
            disabled={status === 'loading' || !file || jd.trim().length < 20}
            fullWidth
            size="large"
            variant="contained"
            color="primary"
            startIcon={status === 'loading' ? <CircularProgress size={18} color="inherit" /> : null}
            sx={{ py: 2, fontSize: 16, letterSpacing: '0.2em' }}
          >
            {status === 'loading' ? '> running_ats_scan...' : '> ./beat_the_bot.sh'}
          </Button>
          <p className="text-center text-xs text-muted-foreground">
            ⚠ Results are AI-generated and may not be fully accurate.
          </p>
        </div>
      </form>

      <Features />
    </>
  );
};

export default LandingView;
