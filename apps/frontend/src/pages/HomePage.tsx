import { Alert, Link, Stack } from "@mui/material";
import { Link as RouterLink } from "react-router";
import { useCurrentUser } from "../auth/useCurrentUser";
import { MetricCard } from "../components/common/MetricCard";
import { Page } from "../components/layout/Page";
import { VacationRequestForm } from "../components/vacation-requests/VacationRequestForm";
import { useCreateVacationRequestMutation } from "../hooks/useCreateVacationRequestMutation";
import { useTasksQuery } from "../hooks/useTasksQuery";
import { useUsersQuery } from "../hooks/useUsersQuery";
import { useVacationRequestsQuery } from "../hooks/useVacationRequestsQuery";

export const HomePage = () => {
  const { currentUser } = useCurrentUser();
  const usersQuery = useUsersQuery();
  const vacationRequestsQuery = useVacationRequestsQuery();
  const tasksQuery = useTasksQuery();
  const createVacationRequestMutation = useCreateVacationRequestMutation();

  const requests = vacationRequestsQuery.data ?? [];
  const tasks = tasksQuery.data ?? [];
  const selectableUsers = (usersQuery.data ?? []).filter((user) => user.id !== currentUser.id);

  const approvedCount = requests.filter((request) => request.status === "GENEHMIGT").length;
  const inProgressCount = requests.filter(
    (request) => request.status === "ANTRAG_GESTELLT" || request.status === "AUTOMATISCHE_PRUEFUNG" || request.status === "VORGESETZTEN_PRUEFUNG",
  ).length;

  return (
    <Page
      title="Miravelo Urlaubsantrag"
      subtitle="Irgendwo zwischen Siebträger, Triathlon-Plänen und dem nächsten Gravel-Bike-Upgrade muss auch bei Miravelo Urlaub planbar bleiben: Anträge schnell stellen, Genehmigungen transparent prüfen und rechtzeitig zur nächsten Feierabendrunde frei bekommen."
    >
      <Stack spacing={3}>
        {(vacationRequestsQuery.error || tasksQuery.error) ? (
          <Alert severity="error">
            {vacationRequestsQuery.error?.message ?? tasksQuery.error?.message}
          </Alert>
        ) : null}

        <Stack spacing={2}>
          <VacationRequestForm
            inline
            isPending={createVacationRequestMutation.isPending}
            users={selectableUsers}
            usersError={usersQuery.error}
            usersPending={usersQuery.isLoading}
            onSubmit={(values) => {
              createVacationRequestMutation.mutate(values);
            }}
          />

          {createVacationRequestMutation.error ? (
            <Alert severity="error">{createVacationRequestMutation.error.message}</Alert>
          ) : null}
        </Stack>

        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={2}
          useFlexGap
          flexWrap="wrap"
          alignItems="stretch"
          sx={{ "& > *": { flex: "1 1 280px", minWidth: 0 } }}
        >
          <MetricCard
            label="Offene Genehmigungen"
            value={tasksQuery.isLoading ? "..." : String(tasks.length)}
            helperText="Alle offenen Genehmigungen"
            action={(
              <Link component={RouterLink} to="/tasks" color="secondary.main" fontWeight={700} underline="hover">
                Offene Genehmigungen anzeigen
              </Link>
            )}
          />
          <MetricCard
            label="Aktive Anträge"
            value={vacationRequestsQuery.isLoading ? "..." : String(inProgressCount)}
            helperText="Offene Anträge"
            action={(
              <Link component={RouterLink} to="/urlaubsantraege" color="secondary.main" fontWeight={700} underline="hover">
                Alle Anträge anzeigen
              </Link>
            )}
          />
          <MetricCard
            label="Genehmigt"
            value={vacationRequestsQuery.isLoading ? "..." : String(approvedCount)}
            helperText="Erfolgreich abgeschlossene Urlaubsanträge."
            action={(
              <Link component={RouterLink} to="/urlaubsantraege" color="secondary.main" fontWeight={700} underline="hover">
                Alle Anträge anzeigen
              </Link>
            )}
          />
        </Stack>
      </Stack>
    </Page>
  );
};
