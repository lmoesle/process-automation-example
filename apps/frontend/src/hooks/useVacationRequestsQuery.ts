import { useQuery } from "@tanstack/react-query";
import { listVacationRequests } from "../api/client";
import { useCurrentUser } from "../auth/useCurrentUser";
import { queryKeys } from "./queryKeys";

export const useVacationRequestsQuery = () => {
  const { currentUser } = useCurrentUser();

  return useQuery({
    queryKey: queryKeys.vacationRequests(currentUser.username),
    queryFn: () => listVacationRequests(currentUser),
  });
};
