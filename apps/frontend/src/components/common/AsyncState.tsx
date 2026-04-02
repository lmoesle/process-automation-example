import type { ReactNode } from "react";
import { Alert, Card, CardContent, LinearProgress, Stack, Typography } from "@mui/material";
import { EmptyState } from "./EmptyState";

type AsyncStateProps = {
  loading: boolean;
  error?: Error | null;
  isEmpty?: boolean;
  emptyTitle: string;
  emptyDescription: string;
  children: ReactNode;
};

export const AsyncState = ({
  loading,
  error,
  isEmpty,
  emptyTitle,
  emptyDescription,
  children,
}: AsyncStateProps) => {
  if (loading) {
    return (
      <Card variant="outlined">
        <CardContent>
          <Stack spacing={2}>
            <Typography variant="h6">Daten werden geladen</Typography>
            <LinearProgress />
          </Stack>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return <Alert severity="error">{error.message}</Alert>;
  }

  if (isEmpty) {
    return <EmptyState title={emptyTitle} description={emptyDescription} />;
  }

  return <>{children}</>;
};
