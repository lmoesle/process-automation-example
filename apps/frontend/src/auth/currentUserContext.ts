import { createContext } from "react";
import type { DemoUser } from "./demoUsers";

export type CurrentUserContextValue = {
  currentUser: DemoUser;
  selectUser: (user: DemoUser) => void;
};

export const CurrentUserContext = createContext<CurrentUserContextValue | undefined>(undefined);
