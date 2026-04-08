import { useEffect } from "react";
import { Alert, Chip, Stack } from "@mui/material";
import { useNavigate, useParams } from "react-router";
import { AsyncState } from "../components/common/AsyncState";
import { Page } from "../components/layout/Page";
import { ManagerDecisionForm } from "../components/tasks/ManagerDecisionForm";
import { TaskDetailsCard } from "../components/tasks/TaskDetailsCard";
import { TaskList } from "../components/tasks/TaskList";
import { useManagerDecisionMutation } from "../hooks/useManagerDecisionMutation";
import { useTaskQuery } from "../hooks/useTaskQuery";
import { useTasksQuery } from "../hooks/useTasksQuery";

export const TasksPage = () => {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const tasksQuery = useTasksQuery();
  const selectedTaskQuery = useTaskQuery(taskId);
  const managerDecisionMutation = useManagerDecisionMutation();

  useEffect(() => {
    if (!taskId && tasksQuery.data?.[0]?.taskId) {
      void navigate(`/tasks/${tasksQuery.data[0].taskId}`, { replace: true });
    }
  }, [navigate, taskId, tasksQuery.data]);

  const selectedTask = selectedTaskQuery.data ?? tasksQuery.data?.find((task) => task.taskId === taskId);

  return (
    <Page
      title="Genehmigungen"
      actions={<Chip color="secondary" label={`${tasksQuery.data?.length ?? 0} Genehmigungen`} />}
    >
      <Stack spacing={3}>
        {managerDecisionMutation.error ? <Alert severity="error">{managerDecisionMutation.error.message}</Alert> : null}

        <Stack direction={{ xs: "column", xl: "row" }} spacing={3} alignItems="flex-start">
          <Stack sx={{ flex: { xs: 1, xl: "0 0 420px" }, width: "100%" }}>
            <AsyncState
              loading={tasksQuery.isLoading}
              error={tasksQuery.error}
              isEmpty={(tasksQuery.data?.length ?? 0) === 0}
              emptyTitle="Keine offenen Genehmigungen"
              emptyDescription=""
            >
              <TaskList
                tasks={tasksQuery.data ?? []}
                selectedTaskId={taskId}
                onSelectTask={(nextTaskId) => {
                  void navigate(`/tasks/${nextTaskId}`);
                }}
              />
            </AsyncState>
          </Stack>

          <Stack sx={{ flex: 1, width: "100%" }} spacing={3}>
            {selectedTaskQuery.error ? <Alert severity="error">{selectedTaskQuery.error.message}</Alert> : null}
            <TaskDetailsCard task={selectedTask} />
            <ManagerDecisionForm
              task={selectedTask}
              isSubmitting={managerDecisionMutation.isPending}
              onSubmitDecision={(body) => {
                if (!selectedTask) {
                  return;
                }

                managerDecisionMutation.mutate(
                  {
                    taskId: selectedTask.taskId,
                    body,
                  },
                  {
                    onSuccess: () => {
                      void navigate("/tasks", { replace: true });
                    },
                  },
                );
              }}
            />
          </Stack>
        </Stack>
      </Stack>
    </Page>
  );
};
