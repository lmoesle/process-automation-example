import { useMutation, useQueryClient } from "@tanstack/react-query";
import { assignTaskToMe } from "../api/client";
import { queryKeys } from "./queryKeys";

export const useAssignTaskToMeMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: assignTaskToMe,
    onSuccess: async (_data, taskId) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.tasks }),
        queryClient.invalidateQueries({ queryKey: queryKeys.task(taskId) }),
      ]);
    },
  });
};
