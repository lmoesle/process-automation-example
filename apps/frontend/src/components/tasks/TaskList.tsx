import { Avatar, Chip, List, ListItemButton, ListItemText, Stack, Typography } from "@mui/material";
import type { UserTask } from "../../api/client";
import { formatDateRange } from "../../util/date";

type TaskListProps = {
  tasks: UserTask[];
  selectedTaskId?: string | undefined;
  onSelectTask: (taskId: string) => void;
  maxItems?: number;
};

export const TaskList = ({ tasks, selectedTaskId, onSelectTask, maxItems }: TaskListProps) => {
  const visibleTasks = typeof maxItems === "number" ? tasks.slice(0, maxItems) : tasks;

  return (
    <List sx={{ p: 0 }}>
      {visibleTasks.map((task) => {
        const subtitle = task.urlaubsantrag
          ? formatDateRange(task.urlaubsantrag.von, task.urlaubsantrag.bis)
          : "Kein Urlaubsantrag im Payload";

        return (
          <ListItemButton
            key={task.taskId}
            selected={task.taskId === selectedTaskId}
            onClick={() => onSelectTask(task.taskId)}
            sx={{ borderRadius: 3, mb: 1, alignItems: "flex-start" }}
          >
            <Avatar sx={{ mr: 2, bgcolor: task.bearbeiter ? "secondary.main" : "primary.main" }}>
              {task.taskId.slice(0, 2).toUpperCase()}
            </Avatar>
            <ListItemText
              primary={
                <Stack direction={{ xs: "column", md: "row" }} spacing={1} alignItems={{ xs: "flex-start", md: "center" }}>
                  <Typography variant="subtitle1">Aufgabe {task.taskId}</Typography>
                  <Chip
                    size="small"
                    color={task.bearbeiter ? "secondary" : "default"}
                    label={task.bearbeiter ? `Bearbeiter: ${task.bearbeiter.name}` : "Unzugewiesen"}
                  />
                </Stack>
              }
              secondary={
                <Stack spacing={0.5} sx={{ mt: 0.75 }}>
                  <Typography color="text.secondary">{subtitle}</Typography>
                  <Typography color="text.secondary">
                    Kandidaten: {task.candidateUsers.map((user) => user.name).join(", ") || "Keine"}
                  </Typography>
                </Stack>
              }
            />
          </ListItemButton>
        );
      })}
    </List>
  );
};
