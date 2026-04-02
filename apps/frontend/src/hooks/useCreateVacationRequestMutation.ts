import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createVacationRequest, type VacationRequestInput } from "../api/client";
import { queryKeys } from "./queryKeys";

export const useCreateVacationRequestMutation = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: VacationRequestInput) => createVacationRequest(input),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.vacationRequests }),
        queryClient.invalidateQueries({ queryKey: queryKeys.tasks }),
      ]);
    },
  });
};
