const Footer = () => {
  return (
    <footer className="border-t border-border/50 py-8 text-center text-xs text-muted-foreground">
      <p>&copy; {new Date().getFullYear()} BeatTheBot // built for humans, tested against bots.</p>
    </footer>
  );
};

export default Footer;
