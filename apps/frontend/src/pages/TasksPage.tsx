import { useEffect } from "react";
import { Alert, Chip, Stack } from "@mui/material";
import { useNavigate, useParams } from "react-router";
import { AsyncState } from "../components/common/AsyncState";
import { Page } from "../components/layout/Page";
import { TaskDecisionDialog } from "../components/tasks/TaskDecisionDialog";
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
  const { reset: resetManagerDecisionMutation } = managerDecisionMutation;

  useEffect(() => {
    resetManagerDecisionMutation();
  }, [resetManagerDecisionMutation, taskId]);

  const selectedTask = selectedTaskQuery.data ?? tasksQuery.data?.find((task) => task.taskId === taskId);
  const selectedTaskIsLoading = Boolean(taskId) && selectedTaskQuery.isLoading && !selectedTask;

  return (
    <Page
      title="Genehmigungen"
      actions={<Chip color="secondary" label={`${tasksQuery.data?.length ?? 0} Genehmigungen`} />}
    >
      <Stack spacing={3}>
        {taskId && !selectedTask && !selectedTaskQuery.isLoading && !selectedTaskQuery.error ? (
          <Alert severity="warning">Die ausgewaehlte Genehmigung wurde nicht gefunden.</Alert>
        ) : null}

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
              resetManagerDecisionMutation();
              void navigate(`/tasks/${nextTaskId}`);
            }}
          />
        </AsyncState>

        <TaskDecisionDialog
          open={Boolean(taskId)}
          task={selectedTask}
          isLoading={selectedTaskIsLoading}
          taskError={selectedTaskQuery.error}
          decisionError={managerDecisionMutation.error}
          isSubmitting={managerDecisionMutation.isPending}
          onClose={() => {
            resetManagerDecisionMutation();
            void navigate("/tasks");
          }}
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
    </Page>
  );
};
