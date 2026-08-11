const Features = () => {
  const items = [
    {
      k: '$ layout_check',
      d: 'Detects multi-column layouts, images, and tables that silently break ATS parsers.',
    },
    {
      k: '$ keyword_match',
      d: 'Compares exact terms from the job description against your resume in context.',
    },
    {
      k: '$ section_audit',
      d: 'Verifies ATS-recognized headers and confirms a dedicated Skills section.',
    },
    {
      k: '$ format_scan',
      d: 'Flags inconsistent date formats and hidden text used to game the system.',
    },
  ];

  return (
    <section className="mt-18">
      <div className="mb-8 text-center">
        <div className="text-xs uppercase tracking-[0.25em] text-primary">// what we scan</div>
        <h2 className="mt-2 text-2xl font-bold text-foreground">
          Your resume, under the microscope.
        </h2>
        <p className="mx-auto mt-3 max-w-2xl text-sm text-muted-foreground">
          We analyze the same signals that can determine whether your resume gets parsed, matched,
          and surfaced by automated screening systems.
        </p>
      </div>
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {items.map((it) => (
          <div
            key={it.k}
            className="rounded-md border border-primary/60 bg-card/40 p-5 transition hover:border-primary hover:bg-primary/5"
          >
            <div className="text-xs font-bold uppercase tracking-widest text-primary">{it.k}</div>
            <p className="mt-3 text-sm leading-relaxed text-muted-foreground">{it.d}</p>
          </div>
        ))}
      </div>
    </section>
  );
};

export default Features;
