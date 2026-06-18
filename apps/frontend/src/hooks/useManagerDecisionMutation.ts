import { useMutation, useQueryClient } from "@tanstack/react-query";
import { submitManagerDecision, type ManagerDecisionInput } from "../api/client";
import { useCurrentUser } from "../auth/useCurrentUser";
import { queryKeys } from "./queryKeys";

type ManagerDecisionPayload = {
  taskId: string;
  body: ManagerDecisionInput;
};

export const useManagerDecisionMutation = () => {
  const queryClient = useQueryClient();
  const { currentUser } = useCurrentUser();

  return useMutation({
    mutationFn: ({ taskId, body }: ManagerDecisionPayload) => submitManagerDecision(currentUser, taskId, body),
    onSuccess: async (_data, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.tasks(currentUser.username) }),
        queryClient.invalidateQueries({ queryKey: queryKeys.task(currentUser.username, variables.taskId) }),
        queryClient.invalidateQueries({ queryKey: queryKeys.vacationRequests(currentUser.username) }),
      ]);
    },
  });
};
