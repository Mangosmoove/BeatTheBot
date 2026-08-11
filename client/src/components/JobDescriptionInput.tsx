import { TextField } from '@mui/material';

interface JobDescriptionInputProps {
  value: string;
  onChange: (value: string) => void;
}

const JobDescriptionInput = ({ value, onChange }: JobDescriptionInputProps) => {
  return (
    <div className="space-y-2">
      <label
        htmlFor="jd"
        className="flex items-center gap-2 text-xs uppercase tracking-widest text-muted-foreground"
      >
        <span className="text-primary">02.</span> job_description.paste
      </label>
      <TextField
        id="jd"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="Paste the job description here..."
        multiline
        minRows={9}
        maxRows={9}
        fullWidth
        helperText={`${value.length} chars`}
        slotProps={{
          formHelperText: {
            sx: { textAlign: 'right', textTransform: 'uppercase', fontSize: 10 },
          },
        }}
      />
    </div>
  );
};

export default JobDescriptionInput;
