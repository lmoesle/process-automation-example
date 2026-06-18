import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createVacationRequest, type VacationRequestInput } from "../api/client";
import { useCurrentUser } from "../auth/useCurrentUser";
import { queryKeys } from "./queryKeys";

export const useCreateVacationRequestMutation = () => {
  const queryClient = useQueryClient();
  const { currentUser } = useCurrentUser();

  return useMutation({
    mutationFn: (input: VacationRequestInput) => createVacationRequest(currentUser, input),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.vacationRequests(currentUser.username) }),
        queryClient.invalidateQueries({ queryKey: queryKeys.tasks(currentUser.username) }),
      ]);
    },
  });
};
