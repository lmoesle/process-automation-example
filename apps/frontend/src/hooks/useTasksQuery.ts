import { useQuery } from "@tanstack/react-query";
import { listTasks } from "../api/client";
import { useCurrentUser } from "../auth/useCurrentUser";
import { queryKeys } from "./queryKeys";

export const useTasksQuery = () => {
  const { currentUser } = useCurrentUser();

  return useQuery({
    queryKey: queryKeys.tasks(currentUser.username),
    queryFn: () => listTasks(currentUser),
  });
};
