import { Card, CardContent, Chip, Divider, Stack, Typography } from "@mui/material";
import type { UserTask } from "../../api/client";
import { formatDateRange } from "../../util/date";
import { formatVacationStatus } from "../../util/status";

type TaskDetailsCardProps = {
  task?: UserTask | undefined;
};

export const TaskDetailsCard = ({ task }: TaskDetailsCardProps) => {
  if (!task) {
    return (
      <Card variant="outlined">
        <CardContent>
          <Typography variant="h6">Keine Genehmigung ausgewählt</Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack spacing={2.5}>
          <Stack spacing={0.5}>
            <Typography variant="h5">Genehmigung {task.taskId}</Typography>
            <Typography color="text.secondary">
              {task.bearbeiter
                ? `Aktueller Bearbeiter: ${task.bearbeiter.name}`
                : "Die Aufgabe ist aktuell unzugewiesen und wird beim Senden der Entscheidung automatisch uebernommen."}
            </Typography>
          </Stack>

          <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
            {task.candidateUsers.map((candidate) => (
              <Chip key={`${task.taskId}-${candidate.email}`} label={candidate.name} variant="outlined" />
            ))}
          </Stack>

          <Divider />

          {task.urlaubsantrag ? (
            <Stack spacing={1.5}>
              <Typography variant="subtitle1">Verknuepfter Urlaubsantrag</Typography>
              <Typography>{formatDateRange(task.urlaubsantrag.von, task.urlaubsantrag.bis)}</Typography>
              <Chip sx={{ alignSelf: "flex-start" }} color={task.urlaubsantrag.status === "GENEHMIGT" ? "success" : task.urlaubsantrag.status === "ABGELEHNT" ? "error" : "warning"} label={formatVacationStatus(task.urlaubsantrag.status)} />
              <Typography color="text.secondary">
                Antragsteller: {task.urlaubsantrag.antragsteller.name} ({task.urlaubsantrag.antragsteller.email})
              </Typography>
              <Typography color="text.secondary">
                Vertretung: {task.urlaubsantrag.vertretung?.name ?? "Keine"}
              </Typography>
              <Stack spacing={1}>
                {task.urlaubsantrag.statusHistorie.map((entry, index) => (
                  <Stack key={`${task.taskId}-${entry.status}-${index}`} direction={{ xs: "column", md: "row" }} spacing={1}>
                    <Chip size="small" variant="outlined" label={formatVacationStatus(entry.status)} />
                    {entry.kommentar?.trim() ? (
                      <Typography color="text.secondary">{entry.kommentar.trim()}</Typography>
                    ) : null}
                  </Stack>
                ))}
              </Stack>
            </Stack>
          ) : (
            <Typography color="text.secondary">Die Aufgabe enthaelt keinen aufgeloesten Urlaubsantrag.</Typography>
          )}
        </Stack>
      </CardContent>
    </Card>
  );
};
