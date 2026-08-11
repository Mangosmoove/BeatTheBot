import { Terminal } from 'lucide-react';

const Header = () => {
  return (
    <header className="border-b border-border/50 bg-background/80 backdrop-blur">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-4">
        <div className="flex items-center gap-2">
          <Terminal className="h-5 w-5 text-primary text-glow" />
          <span className="text-lg font-bold tracking-tight">
            <span className="text-primary text-glow">BeatThe</span>
            <span className="text-foreground">Bot</span>
            <span className="ml-1 text-primary cursor-blink">_</span>
          </span>
        </div>
        <div className="hidden text-xs text-muted-foreground md:block">
          status: <span className="text-success">online</span>
        </div>
      </div>
    </header>
  );
};

export default Header;
