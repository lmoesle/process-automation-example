import { StrictMode } from "react";
import ReactDOM from "react-dom/client";
import { QueryClientProvider } from "@tanstack/react-query";
import { ReactQueryDevtools } from "@tanstack/react-query-devtools";
import { CssBaseline, ThemeProvider } from "@mui/material";
import { HashRouter } from "react-router";
import { queryClient } from "./config/queryClient";
import { AppRoutes } from "./components/layout/AppRoutes";
import { CurrentUserProvider } from "./auth/CurrentUserProvider";
import { theme } from "./theme";
import "./index.css";

const rootElement = document.getElementById("root");

if (!rootElement) {
  throw new Error("Root element #root wurde nicht gefunden.");
}

ReactDOM.createRoot(rootElement).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <CurrentUserProvider>
        <ThemeProvider theme={theme}>
          <CssBaseline />
          <HashRouter>
            <AppRoutes />
          </HashRouter>
        </ThemeProvider>
      </CurrentUserProvider>
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  </StrictMode>,
);
