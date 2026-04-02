import { Card, CardContent, Chip, Divider, Stack, Typography } from "@mui/material";
import type { VacationRequest } from "../../api/client";
import { formatDateRange } from "../../util/date";
import { formatVacationStatus } from "../../util/status";

type VacationRequestListProps = {
  requests: VacationRequest[];
  maxItems?: number;
};

export const VacationRequestList = ({ requests, maxItems }: VacationRequestListProps) => {
  const visibleRequests = typeof maxItems === "number" ? requests.slice(0, maxItems) : requests;

  return (
    <Stack spacing={2}>
      {visibleRequests.map((request) => (
        <Card key={request.id} variant="outlined">
          <CardContent>
            <Stack spacing={2}>
              <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" spacing={1}>
                <Stack spacing={0.5}>
                  <Typography variant="h6">{formatDateRange(request.von, request.bis)}</Typography>
                  <Typography color="text.secondary">Antrag-ID: {request.id}</Typography>
                </Stack>
                <Chip color={request.status === "GENEHMIGT" ? "success" : request.status === "ABGELEHNT" ? "error" : "warning"} label={formatVacationStatus(request.status)} />
              </Stack>

              <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
                <Stack spacing={0.5} sx={{ flex: 1 }}>
                  <Typography variant="subtitle2">Antragsteller</Typography>
                  <Typography>{request.antragsteller.name}</Typography>
                  <Typography color="text.secondary">{request.antragsteller.email}</Typography>
                </Stack>

                <Stack spacing={0.5} sx={{ flex: 1 }}>
                  <Typography variant="subtitle2">Vertretung</Typography>
                  <Typography>{request.vertretung?.name ?? "Keine Vertretung hinterlegt"}</Typography>
                  <Typography color="text.secondary">{request.vertretung?.email ?? " "}</Typography>
                </Stack>

                <Stack spacing={0.5} sx={{ flex: 1 }}>
                  <Typography variant="subtitle2">Vorgesetzter</Typography>
                  <Typography>{request.vorgesetzter?.name ?? "Noch nicht zugeordnet"}</Typography>
                  <Typography color="text.secondary">{request.vorgesetzter?.email ?? " "}</Typography>
                </Stack>
              </Stack>

              <Divider />

              <Stack spacing={1}>
                <Typography variant="subtitle2">Statushistorie</Typography>
                {request.statusHistorie.map((entry, index) => (
                  <Stack key={`${request.id}-${entry.status}-${index}`} direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "flex-start", md: "center" }}>
                    <Chip size="small" variant="outlined" label={formatVacationStatus(entry.status)} />
                    {entry.kommentar?.trim() ? (
                      <Typography color="text.secondary">{entry.kommentar.trim()}</Typography>
                    ) : null}
                  </Stack>
                ))}
              </Stack>
            </Stack>
          </CardContent>
        </Card>
      ))}

      {typeof maxItems === "number" && requests.length > visibleRequests.length ? (
        <Typography color="text.secondary">Es werden {visibleRequests.length} von {requests.length} Antraegen angezeigt.</Typography>
      ) : null}
    </Stack>
  );
};
