import { useQuery } from "@tanstack/react-query";
import { listTasks } from "../api/client";
import { queryKeys } from "./queryKeys";

export const useTasksQuery = () =>
  useQuery({
    queryKey: queryKeys.tasks,
    queryFn: listTasks,
  });
