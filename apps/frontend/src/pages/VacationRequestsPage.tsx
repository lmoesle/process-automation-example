import { Alert, Chip, Stack } from "@mui/material";
import { AsyncState } from "../components/common/AsyncState";
import { Page } from "../components/layout/Page";
import { VacationRequestForm } from "../components/vacation-requests/VacationRequestForm";
import { VacationRequestList } from "../components/vacation-requests/VacationRequestList";
import { runtimeConfig } from "../config/runtimeConfig";
import { useCreateVacationRequestMutation } from "../hooks/useCreateVacationRequestMutation";
import { useVacationRequestsQuery } from "../hooks/useVacationRequestsQuery";

export const VacationRequestsPage = () => {
  const vacationRequestsQuery = useVacationRequestsQuery();
  const createVacationRequestMutation = useCreateVacationRequestMutation();

  return (
    <Page
      title="Urlaubsanträge"
      actions={<Chip color="secondary" label={`${vacationRequestsQuery.data?.length ?? 0} Eintraege`} />}
    >
      <Stack direction={{ xs: "column", xl: "row" }} spacing={3} alignItems="flex-start">
        <Stack sx={{ flex: { xs: 1, xl: "0 0 360px" }, width: "100%" }}>
          <VacationRequestForm
            isPending={createVacationRequestMutation.isPending}
            onSubmit={(values) => {
              createVacationRequestMutation.mutate(
                values.vertretungId
                  ? {
                      antragstellerId: runtimeConfig.applicantId,
                      bis: values.bis,
                      von: values.von,
                      vertretungId: values.vertretungId,
                    }
                  : {
                      antragstellerId: runtimeConfig.applicantId,
                      bis: values.bis,
                      von: values.von,
                    },
              );
            }}
          />

          {createVacationRequestMutation.error ? (
            <Alert severity="error" sx={{ mt: 2 }}>
              {createVacationRequestMutation.error.message}
            </Alert>
          ) : null}
        </Stack>

        <Stack sx={{ flex: 1, width: "100%" }} spacing={2}>
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
