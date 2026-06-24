import { Alert, Dialog, DialogContent, DialogTitle, Stack, Typography } from "@mui/material";
import type { ManagerDecisionInput, UserTask } from "../../api/client";
import { ManagerDecisionForm } from "./ManagerDecisionForm";
import { TaskDetailsCard } from "./TaskDetailsCard";

type TaskDecisionDialogProps = {
  open: boolean;
  task?: UserTask | undefined;
  isLoading: boolean;
  taskError?: Error | null;
  decisionError?: Error | null;
  isSubmitting: boolean;
  onClose: () => void;
  onSubmitDecision: (body: ManagerDecisionInput) => void;
};

export const TaskDecisionDialog = ({
  open,
  task,
  isLoading,
  taskError,
  decisionError,
  isSubmitting,
  onClose,
  onSubmitDecision,
}: TaskDecisionDialogProps) => (
  <Dialog fullWidth maxWidth="md" open={open} onClose={onClose} aria-labelledby="task-decision-dialog-title">
    <DialogTitle id="task-decision-dialog-title">Genehmigung bearbeiten</DialogTitle>
    <DialogContent dividers>
      <Stack spacing={3} sx={{ py: 1 }}>
        {taskError ? <Alert severity="error">{taskError.message}</Alert> : null}
        {decisionError ? <Alert severity="error">{decisionError.message}</Alert> : null}
        {isLoading ? <Typography color="text.secondary">Genehmigung wird geladen...</Typography> : null}
        {!isLoading && !taskError ? (
          <>
            <TaskDetailsCard task={task} />
            <ManagerDecisionForm task={task} isSubmitting={isSubmitting} onSubmitDecision={onSubmitDecision} />
          </>
        ) : null}
      </Stack>
    </DialogContent>
  </Dialog>
);
