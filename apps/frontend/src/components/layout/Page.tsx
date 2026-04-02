import type { ReactNode } from "react";
import { Stack, Typography } from "@mui/material";

type PageProps = {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
  children: ReactNode;
};

export const Page = ({ title, subtitle, actions, children }: PageProps) => (
  <Stack spacing={3}>
    <Stack
      direction={{ xs: "column", md: "row" }}
      spacing={2}
      justifyContent="space-between"
      alignItems={{ xs: "flex-start", md: "center" }}
    >
      <Stack spacing={0.5}>
        <Typography variant="h3">{title}</Typography>
        {subtitle ? (
          <Typography color="text.secondary" sx={{ maxWidth: 760 }}>
            {subtitle}
          </Typography>
        ) : null}
      </Stack>
      {actions}
    </Stack>
    {children}
  </Stack>
);
