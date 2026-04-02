import AssignmentTurnedInRoundedIcon from "@mui/icons-material/AssignmentTurnedInRounded";
import BeachAccessRoundedIcon from "@mui/icons-material/BeachAccessRounded";
import TaskRoundedIcon from "@mui/icons-material/TaskRounded";
import { Alert, Card, CardContent, Stack, Typography } from "@mui/material";
import { Link as RouterLink, useNavigate } from "react-router";
import { MetricCard } from "../components/common/MetricCard";
import { Page } from "../components/layout/Page";
import { TaskList } from "../components/tasks/TaskList";
import { VacationRequestList } from "../components/vacation-requests/VacationRequestList";
import { useTasksQuery } from "../hooks/useTasksQuery";
import { useVacationRequestsQuery } from "../hooks/useVacationRequestsQuery";

export const HomePage = () => {
  const navigate = useNavigate();
  const vacationRequestsQuery = useVacationRequestsQuery();
  const tasksQuery = useTasksQuery();

  const requests = vacationRequestsQuery.data ?? [];
  const tasks = tasksQuery.data ?? [];

  const approvedCount = requests.filter((request) => request.status === "GENEHMIGT").length;
  const inProgressCount = requests.filter(
    (request) => request.status === "ANTRAG_GESTELLT" || request.status === "AUTOMATISCHE_PRUEFUNG" || request.status === "VORGESETZTEN_PRUEFUNG",
  ).length;

  return (
    <Page title="Übersicht">
      <Stack spacing={3}>
        {(vacationRequestsQuery.error || tasksQuery.error) ? (
          <Alert severity="error">
            {vacationRequestsQuery.error?.message ?? tasksQuery.error?.message}
          </Alert>
        ) : null}

        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={2}
          useFlexGap
          flexWrap="wrap"
          sx={{ "& > *": { flex: "1 1 280px" } }}
        >
          <MetricCard
            label="Offene Genehmigungen"
            value={tasksQuery.isLoading ? "..." : String(tasks.length)}
            helperText="Alle offenen Genehmigungen"
            icon={<TaskRoundedIcon color="primary" />}
          />
          <MetricCard
            label="Aktive Anträge"
            value={vacationRequestsQuery.isLoading ? "..." : String(inProgressCount)}
            helperText="Offene Anträge"
            icon={<BeachAccessRoundedIcon color="secondary" />}
          />
          <MetricCard
            label="Genehmigt"
            value={vacationRequestsQuery.isLoading ? "..." : String(approvedCount)}
            helperText="Bereits erfolgreich abgeschlossene Urlaubsanträge."
            icon={<AssignmentTurnedInRoundedIcon color="success" />}
          />
        </Stack>

        <Stack direction={{ xs: "column", xl: "row" }} spacing={3} alignItems="stretch">
          <Card variant="outlined" sx={{ flex: 1 }}>
            <CardContent>
              <Stack spacing={2}>
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                  <Typography variant="h5">Neueste Anträge</Typography>
                  <Typography component={RouterLink} to="/urlaubsantraege" color="secondary.main">
                    Alle anzeigen
                  </Typography>
                </Stack>
                <VacationRequestList requests={requests} maxItems={3} />
              </Stack>
            </CardContent>
          </Card>

          <Card variant="outlined" sx={{ flex: 1 }}>
            <CardContent>
              <Stack spacing={2}>
                <Stack direction="row" justifyContent="space-between" alignItems="center">
                  <Typography variant="h5">Aktuelle Genehmigungen</Typography>
                  <Typography component={RouterLink} to="/tasks" color="secondary.main">
                    Meine Genehmigungen
                  </Typography>
                </Stack>
                <TaskList
                  tasks={tasks}
                  maxItems={4}
                  onSelectTask={(taskId) => {
                    void navigate(`/tasks/${taskId}`);
                  }}
                />
              </Stack>
            </CardContent>
          </Card>
        </Stack>
      </Stack>
    </Page>
  );
};
