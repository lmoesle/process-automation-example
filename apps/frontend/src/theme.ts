import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    primary: {
      main: "#005b4f",
      dark: "#003f37",
      light: "#3c8b80",
      contrastText: "#ffffff",
    },
    secondary: {
      main: "#c4673a",
      dark: "#9f4d27",
      light: "#df8a61",
      contrastText: "#ffffff",
    },
    background: {
      default: "transparent",
      paper: "rgba(255, 250, 243, 0.88)",
    },
    text: {
      primary: "#14231d",
      secondary: "#4f6059",
    },
    success: {
      main: "#2f8f52",
    },
    warning: {
      main: "#c98218",
    },
    error: {
      main: "#b83d2f",
    },
  },
  shape: {
    borderRadius: 20,
  },
  typography: {
    fontFamily: '"IBM Plex Sans", "Roboto", "Helvetica Neue", Arial, sans-serif',
    h1: {
      fontWeight: 700,
      letterSpacing: "-0.03em",
    },
    h2: {
      fontWeight: 700,
      letterSpacing: "-0.03em",
    },
    h3: {
      fontWeight: 700,
      letterSpacing: "-0.03em",
    },
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          backdropFilter: "blur(18px)",
          boxShadow: "0 20px 60px rgba(20, 35, 29, 0.08)",
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backdropFilter: "blur(18px)",
        },
      },
    },
  },
});
