import { useMutation, useQueryClient } from "@tanstack/react-query";
import { submitManagerDecision, type ManagerDecisionInput } from "../api/client";
import { queryKeys } from "./queryKeys";

type ManagerDecisionPayload = {
  taskId: string;
  body: ManagerDecisionInput;
};

export const useManagerDecisionMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ taskId, body }: ManagerDecisionPayload) => submitManagerDecision(taskId, body),
    onSuccess: async (_data, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.tasks }),
        queryClient.invalidateQueries({ queryKey: queryKeys.task(variables.taskId) }),
        queryClient.invalidateQueries({ queryKey: queryKeys.vacationRequests }),
      ]);
    },
  });
};
