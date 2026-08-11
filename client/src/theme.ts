import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#5bf24f', // --primary
      contrastText: '#020703', // --primary-foreground
    },
    secondary: {
      main: '#101f13', // --secondary
      contrastText: '#7cff85', // --secondary-foreground
    },
    error: {
      main: '#ff343f', // --destructive
      contrastText: '#f8f8f8', // --destructive-foreground
    },
    background: {
      default: '#040b05', // --background
      paper: '#0b140d', // --card
    },
    text: {
      primary: '#8aff91', // --foreground
      secondary: '#679f69', // --muted-foreground
    },
  },
  typography: {
    fontFamily: '"JetBrains Mono", ui-monospace, SFMono-Regular, Menlo, monospace',
  },
});
