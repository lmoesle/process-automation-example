import type { ReactNode } from "react";
import { alpha } from "@mui/material/styles";
import { Card, CardContent, Stack, Typography } from "@mui/material";

type MetricCardProps = {
  label: string;
  value: string;
  helperText: string;
  icon: ReactNode;
};

export const MetricCard = ({ label, value, helperText, icon }: MetricCardProps) => (
  <Card
    sx={(theme) => ({
      position: "relative",
      overflow: "hidden",
      background: `linear-gradient(145deg, ${alpha(theme.palette.background.paper, 0.98)} 0%, ${alpha(
        theme.palette.common.white,
        0.78,
      )} 100%)`,
      "&::after": {
        content: '""',
        position: "absolute",
        inset: "auto -10% -28% auto",
        width: 120,
        height: 120,
        borderRadius: "50%",
        backgroundColor: alpha(theme.palette.secondary.main, 0.12),
      },
    })}
  >
    <CardContent>
      <Stack spacing={2}>
        <Stack direction="row" justifyContent="space-between" alignItems="flex-start">
          <Typography variant="overline" sx={{ color: "text.secondary", letterSpacing: "0.08em" }}>
            {label}
          </Typography>
          {icon}
        </Stack>
        <Typography variant="h3">{value}</Typography>
        <Typography color="text.secondary">{helperText}</Typography>
      </Stack>
    </CardContent>
  </Card>
);
