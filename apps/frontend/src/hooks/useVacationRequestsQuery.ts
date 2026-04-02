import { useQuery } from "@tanstack/react-query";
import { listVacationRequests } from "../api/client";
import { queryKeys } from "./queryKeys";

export const useVacationRequestsQuery = () =>
  useQuery({
    queryKey: queryKeys.vacationRequests,
    queryFn: listVacationRequests,
  });
