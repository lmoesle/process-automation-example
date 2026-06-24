import { Alert, Chip, Stack } from "@mui/material";
import { AsyncState } from "../components/common/AsyncState";
import { Page } from "../components/layout/Page";
import { VacationRequestForm } from "../components/vacation-requests/VacationRequestForm";
import { VacationRequestList } from "../components/vacation-requests/VacationRequestList";
import { useCurrentUser } from "../auth/useCurrentUser";
import { useCreateVacationRequestMutation } from "../hooks/useCreateVacationRequestMutation";
import { useUsersQuery } from "../hooks/useUsersQuery";
import { useVacationRequestsQuery } from "../hooks/useVacationRequestsQuery";

export const VacationRequestsPage = () => {
  const { currentUser } = useCurrentUser();
  const usersQuery = useUsersQuery();
  const vacationRequestsQuery = useVacationRequestsQuery();
  const createVacationRequestMutation = useCreateVacationRequestMutation();
  const selectableUsers = (usersQuery.data ?? []).filter((user) => user.id !== currentUser.id);

  return (
    <Page
      title="Urlaubsanträge"
      actions={<Chip color="secondary" label={`${vacationRequestsQuery.data?.length ?? 0} Eintraege`} />}
    >
      <Stack spacing={3}>
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
            <Alert severity="error" sx={{ mt: 2 }}>
              {createVacationRequestMutation.error.message}
            </Alert>
          ) : null}
        </Stack>

        <Stack sx={{ width: "100%" }} spacing={2}>
          <AsyncState
            loading={vacationRequestsQuery.isLoading}
            error={vacationRequestsQuery.error}
            isEmpty={(vacationRequestsQuery.data?.length ?? 0) === 0}
            emptyTitle="Noch keine Urlaubsanträge"
            emptyDescription=""
          >
            <VacationRequestList requests={vacationRequestsQuery.data ?? []} />
          </AsyncState>
        </Stack>
      </Stack>
    </Page>
  );
};
