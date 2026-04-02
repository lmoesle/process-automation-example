import { useQuery } from "@tanstack/react-query";
import { getTask } from "../api/client";
import { queryKeys } from "./queryKeys";

export const useTaskQuery = (taskId?: string) =>
  useQuery({
    queryKey: taskId ? queryKeys.task(taskId) : [...queryKeys.tasks, "selected"] as const,
    queryFn: () => getTask(taskId ?? ""),
    enabled: Boolean(taskId),
  });
