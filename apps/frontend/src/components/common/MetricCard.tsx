import type { ReactNode } from "react";
import { alpha } from "@mui/material/styles";
import { Card, CardContent, Stack, Typography } from "@mui/material";

type MetricCardProps = {
  label: string;
  value: string;
  helperText: string;
  action?: ReactNode;
};

export const MetricCard = ({ label, value, helperText, action }: MetricCardProps) => (
  <Card
    sx={(theme) => ({
      position: "relative",
      overflow: "hidden",
      height: "100%",
      backgroundColor: "#F9F7F7",
      "&::after": {
        content: '""',
        position: "absolute",
        inset: "auto -10% -28% auto",
        width: 120,
        height: 120,
        borderRadius: "50%",
        backgroundColor: alpha(theme.palette.secondary.main, 0.12),
        pointerEvents: "none",
      },
    })}
  >
    <CardContent sx={{ height: "100%", position: "relative", zIndex: 1 }}>
      <Stack spacing={2} sx={{ height: "100%" }}>
        <Typography variant="overline" sx={{ color: "text.secondary", letterSpacing: "0.08em" }}>
          {label}
        </Typography>
        <Typography variant="h3">{value}</Typography>
        <Typography color="text.secondary">{helperText}</Typography>
        {action ? <Stack sx={{ mt: "auto", pt: 1 }}>{action}</Stack> : null}
      </Stack>
    </CardContent>
  </Card>
);
