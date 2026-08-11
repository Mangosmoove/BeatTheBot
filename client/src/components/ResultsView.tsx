import type { Application, AiSections } from '../types/Application';
import { CheckCircle2, XCircle, RotateCcw } from 'lucide-react';

interface ResultsViewProps {
  result: Application;
  onResubmit: () => void;
}

const ResultsView = ({ result, onResubmit }: ResultsViewProps) => {
  let sections: AiSections = {};
  try {
    sections = JSON.parse(result.aiSections);
  } catch {
    // aiSections may be the error payload set in ApplicationController's catch block
  }

  const hasError = Object.keys(sections).length === 0;

  return (
    <div className="space-y-8">
      <div className="text-center">
        <div className="text-xs uppercase tracking-[0.25em] text-primary">// scan_complete</div>
        <div className="mt-2 text-6xl font-bold text-foreground">
          {result.aiScore}
          <span className="text-2xl text-muted-foreground">/100</span>
        </div>
      </div>

      {hasError ? (
        <p className="text-center text-sm text-muted-foreground">
          Something went wrong scoring this resume. Try resubmitting.
        </p>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {Object.entries(sections).map(([categoryName, category]) => (
            <div key={categoryName} className="rounded-md border border-primary/60 bg-card/40 p-5">
              <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-widest text-primary">
                {category.passed ? (
                  <CheckCircle2 className="h-4 w-4 text-success" />
                ) : (
                  <XCircle className="h-4 w-4 text-destructive" />
                )}
                {categoryName}
              </div>
              <ul className="mt-3 space-y-2">
                {Object.entries(category.checks).map(([checkName, check]) => (
                  <li key={checkName} className="text-sm">
                    <div className="flex items-center gap-2 font-medium text-foreground">
                      {check.passed ? (
                        <CheckCircle2 className="h-3.5 w-3.5 text-success shrink-0" />
                      ) : (
                        <XCircle className="h-3.5 w-3.5 text-destructive shrink-0" />
                      )}
                      {checkName.replace(/_/g, ' ')}
                    </div>
                    <p className="ml-5.5 mt-0.5 text-muted-foreground">{check.notes}</p>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}

      <div className="text-center">
        <button
          onClick={onResubmit}
          className="inline-flex items-center gap-2 text-sm text-primary hover:underline"
        >
          <RotateCcw className="h-4 w-4" />
          try again
        </button>
      </div>
    </div>
  );
};

export default ResultsView;
