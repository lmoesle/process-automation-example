import { Box } from "@mui/material";
import { Outlet } from "react-router";
import { Navbar } from "./Navbar";

export const AppShell = () => (
  <Box sx={{ minHeight: "100vh" }}>
    <Box
      aria-hidden
      sx={{
        position: "fixed",
        inset: 0,
        zIndex: -1,
        background:
          "radial-gradient(circle at top right, rgba(196, 103, 58, 0.16), transparent 28%), radial-gradient(circle at bottom left, rgba(0, 91, 79, 0.12), transparent 32%)",
      }}
    />
    <Navbar />
    <Box
      component="main"
      sx={{
        mx: "auto",
        width: "min(100%, 1280px)",
        px: { xs: 2, md: 4 },
        py: { xs: 3, md: 4 },
      }}
    >
      <Outlet />
    </Box>
  </Box>
);
