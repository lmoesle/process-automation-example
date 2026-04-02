import { Box, Button, Stack, Typography } from "@mui/material";
import { alpha } from "@mui/material/styles";
import { Link as RouterLink, useLocation } from "react-router";

const navigationItems = [
  { label: "Übersicht", to: "/" },
  { label: "Urlaubsanträge", to: "/urlaubsantraege" },
  { label: "Genehmigungen", to: "/tasks" },
];

const isActive = (pathname: string, target: string) => {
  if (target === "/") {
    return pathname === "/";
  }

  return pathname.startsWith(target);
};

export const Navbar = () => {
  const location = useLocation();

  return (
    <Box
      component="header"
      sx={(theme) => ({
        position: "sticky",
        top: 0,
        zIndex: theme.zIndex.appBar,
        borderBottom: `1px solid ${alpha(theme.palette.primary.main, 0.12)}`,
        backdropFilter: "blur(18px)",
        backgroundColor: alpha("#fff9f1", 0.72),
      })}
    >
      <Box
        sx={{
          mx: "auto",
          width: "min(100%, 1280px)",
          px: { xs: 2, md: 4 },
          py: 2,
        }}
      >
        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={2}
          alignItems={{ xs: "flex-start", md: "center" }}
          justifyContent="space-between"
        >
          <Stack spacing={0.5}>
            <Typography variant="overline" sx={{ color: "secondary.main", letterSpacing: "0.14em" }}>
              Process Automation Example
            </Typography>
          </Stack>

          <Stack direction="row" spacing={1} sx={{ overflowX: "auto", pb: { xs: 0.5, md: 0 } }}>
            {navigationItems.map((item) => {
              const active = isActive(location.pathname, item.to);

              return (
                <Button
                  key={item.to}
                  component={RouterLink}
                  to={item.to}
                  color={active ? "secondary" : "primary"}
                  variant={active ? "contained" : "text"}
                  sx={{ whiteSpace: "nowrap" }}
                >
                  {item.label}
                </Button>
              );
            })}
          </Stack>
        </Stack>
      </Box>
    </Box>
  );
};
