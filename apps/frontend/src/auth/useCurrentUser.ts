import { useContext } from "react";
import { CurrentUserContext } from "./currentUserContext";

export const useCurrentUser = () => {
  const context = useContext(CurrentUserContext);

  if (!context) {
    throw new Error("useCurrentUser muss innerhalb des CurrentUserProvider verwendet werden.");
  }

  return context;
};
