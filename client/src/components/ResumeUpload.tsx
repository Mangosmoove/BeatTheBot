import { FileText, Upload, X } from 'lucide-react';
import { IconButton } from '@mui/material';
import { useRef, useState, type DragEvent } from 'react';

interface ResumeUploadProps {
  file: File | null;
  onFileChange: (file: File | null) => void;
  disabled?: boolean;
}

const ResumeUpload = ({ file, onFileChange, disabled = false }: ResumeUploadProps) => {
  const [dragOver, setDragOver] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const onDrop = (e: DragEvent<HTMLLabelElement>) => {
    e.preventDefault();
    setDragOver(false);
    if (disabled) return;
    onFileChange(e.dataTransfer.files?.[0] ?? null);
  };

  return (
    <div className="space-y-2">
      <label className="flex items-center gap-2 text-xs uppercase tracking-widest text-muted-foreground">
        <span className="text-primary">01.</span> resume.upload
      </label>
      <label
        onDragOver={(e) => {
          e.preventDefault();
          if (!disabled) setDragOver(true);
        }}
        onDragLeave={() => setDragOver(false)}
        onDrop={onDrop}
        className={`group relative flex h-64 flex-col items-center justify-center gap-3 rounded-md border-2 border-dashed p-6 text-center transition ${
          disabled
            ? 'cursor-not-allowed opacity-50 border-border bg-card/40'
            : 'cursor-pointer border-border bg-card/40 hover:border-primary/60 hover:bg-primary/5'
        } ${dragOver && !disabled ? 'border-primary bg-primary/10 border-glow' : ''}`}
      >
        <input
          ref={inputRef}
          type="file"
          accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
          className="hidden"
          disabled={disabled}
          onChange={(e) => onFileChange(e.target.files?.[0] ?? null)}
        />
        {file ? (
          <>
            <FileText className="h-10 w-10 text-primary text-glow" />
            <div className="max-w-full truncate text-sm font-semibold text-foreground">
              {file.name}
            </div>
            <div className="text-xs text-muted-foreground">
              {(file.size / 1024).toFixed(1)} KB · click to change
            </div>
            <IconButton
              size="small"
              disabled={disabled}
              onClick={(e) => {
                e.preventDefault();
                e.stopPropagation();
                onFileChange(null);
                if (inputRef.current) inputRef.current.value = '';
              }}
              className="absolute! right-2! top-2!"
              aria-label="Remove file"
            >
              <X className="h-4 w-4" />
            </IconButton>
          </>
        ) : (
          <>
            <Upload className="h-10 w-10 text-primary" />
            <div className="text-sm font-semibold text-foreground">Drop your resume here</div>
            <div className="text-xs text-muted-foreground">.pdf or .docx · up to 10MB</div>
          </>
        )}
      </label>
    </div>
  );
};

export default ResumeUpload;
