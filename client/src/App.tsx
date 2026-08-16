import { useState } from 'react';
import Header from './components/Header';
import Footer from './components/Footer';
import LandingView from './components/LandingView';
import ResultsView from './components/ResultsView';
import type { Application } from './types/Application';

function App() {
  const [result, setResult] = useState<Application | null>(null);

  const resetToLanding = () => {
    setResult(null);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="min-h-screen">
      <Header />
      <main className="mx-auto max-w-6xl px-4 py-10 md:py-16">
        {!result ? (
          <LandingView onResult={setResult} />
        ) : (
          <ResultsView result={result} onResubmit={resetToLanding} />
        )}
      </main>
      <Footer />
    </div>
  );
}

export default App;
