import { useQuery } from "@tanstack/react-query";
import { listUsers } from "../api/client";
import { useCurrentUser } from "../auth/useCurrentUser";
import { queryKeys } from "./queryKeys";

export const useUsersQuery = () => {
  const { currentUser } = useCurrentUser();

  return useQuery({
    queryKey: queryKeys.users(currentUser.username),
    queryFn: () => listUsers(currentUser),
  });
};
