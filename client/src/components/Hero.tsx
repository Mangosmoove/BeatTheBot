const Hero = () => {
  return (
    <section className="text-center">
      <h1 className="text-balance text-5xl font-bold leading-[1.05] tracking-tight md:text-7xl">
        <span className="text-foreground">Beat the </span>
        <span className="text-primary text-glow">bot.</span>
        <br />
        <span className="text-muted-foreground">Land the interview.</span>
      </h1>
      <p className="mx-auto mt-6 max-w-2xl text-balance text-base text-muted-foreground md:text-lg">
        We reverse-engineer the ATS so your resume reaches a human. Drop your resume, paste the job,
        and see what the bot sees.
      </p>
    </section>
  );
};

export default Hero;
