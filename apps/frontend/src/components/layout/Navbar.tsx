import { useState } from "react";
import { Avatar, Box, Button, IconButton, ListItemAvatar, ListItemText, Menu, MenuItem, Stack, Typography } from "@mui/material";
import { alpha } from "@mui/material/styles";
import { Link as RouterLink, useLocation } from "react-router";
import { demoUsers } from "../../auth/demoUsers";
import { useCurrentUser } from "../../auth/useCurrentUser";

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
  const { currentUser, selectUser } = useCurrentUser();
  const [userMenuAnchor, setUserMenuAnchor] = useState<HTMLElement | null>(null);
  const userMenuOpen = Boolean(userMenuAnchor);

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
              Miravelo Urlaubsantrag
            </Typography>
          </Stack>

          <Stack
            direction="row"
            spacing={1.5}
            alignItems="center"
            sx={{ alignSelf: { xs: "stretch", md: "center" }, width: { xs: "100%", md: "auto" } }}
          >
            <Stack direction="row" spacing={1} sx={{ flex: 1, overflowX: "auto", pb: { xs: 0.5, md: 0 } }}>
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

            <IconButton
              aria-label="Benutzer wechseln"
              aria-controls={userMenuOpen ? "user-menu" : undefined}
              aria-haspopup="menu"
              aria-expanded={userMenuOpen ? "true" : undefined}
              onClick={(event) => setUserMenuAnchor(event.currentTarget)}
              sx={{ p: 0 }}
            >
              <Avatar sx={{ bgcolor: "secondary.main", color: "secondary.contrastText" }}>
                {currentUser.initials}
              </Avatar>
            </IconButton>

            <Menu
              id="user-menu"
              anchorEl={userMenuAnchor}
              open={userMenuOpen}
              onClose={() => setUserMenuAnchor(null)}
              anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
              transformOrigin={{ vertical: "top", horizontal: "right" }}
            >
              {demoUsers.map((user) => (
                <MenuItem
                  key={user.username}
                  selected={user.username === currentUser.username}
                  onClick={() => {
                    selectUser(user);
                    setUserMenuAnchor(null);
                  }}
                >
                  <ListItemAvatar>
                    <Avatar sx={{ bgcolor: "primary.main", color: "primary.contrastText" }}>{user.initials}</Avatar>
                  </ListItemAvatar>
                  <ListItemText primary={user.name} secondary={user.username} />
                </MenuItem>
              ))}
            </Menu>
          </Stack>
        </Stack>
      </Box>
    </Box>
  );
};
