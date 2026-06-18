import { useQuery } from "@tanstack/react-query";
import { getTask } from "../api/client";
import { useCurrentUser } from "../auth/useCurrentUser";
import { queryKeys } from "./queryKeys";

export const useTaskQuery = (taskId?: string) => {
  const { currentUser } = useCurrentUser();

  return useQuery({
    queryKey: taskId
      ? queryKeys.task(currentUser.username, taskId)
      : [...queryKeys.tasks(currentUser.username), "selected"] as const,
    queryFn: () => getTask(currentUser, taskId ?? ""),
    enabled: Boolean(taskId),
  });
};
