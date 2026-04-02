import { Card, CardContent, Stack, Typography } from "@mui/material";

type EmptyStateProps = {
  title: string;
  description: string;
};

export const EmptyState = ({ title, description }: EmptyStateProps) => (
  <Card variant="outlined">
    <CardContent>
      <Stack spacing={1}>
        <Typography variant="h6">{title}</Typography>
        <Typography color="text.secondary">{description}</Typography>
      </Stack>
    </CardContent>
  </Card>
);
