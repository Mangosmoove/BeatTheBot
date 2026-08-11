export interface CheckResult {
  passed: boolean;
  notes: string;
}

export interface CategoryResult {
  passed: boolean;
  checks: Record<string, CheckResult>;
}

export type AiSections = Record<string, CategoryResult>;

export interface Job {
  id: number;
  description: string;
}

export interface Application {
  id: number;
  job: Job;
  sessionToken: string;
  resumeText: string;
  aiScore: number;
  aiSections: string;
  submittedAt: string;
}
