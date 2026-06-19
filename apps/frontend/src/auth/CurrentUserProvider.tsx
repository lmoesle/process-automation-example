import { type ReactNode, useState } from "react";
import { type DemoUser, loadSelectedUser, storeSelectedUser } from "./demoUsers";
import { CurrentUserContext } from "./currentUserContext";

type CurrentUserProviderProps = {
  children: ReactNode;
};

export const CurrentUserProvider = ({ children }: CurrentUserProviderProps) => {
  const [currentUser, setCurrentUser] = useState(loadSelectedUser);

  const selectUser = (user: DemoUser) => {
    // Demo-only user switcher: persists the selected sample user, not a real login session.
    storeSelectedUser(user);
    setCurrentUser(user);
  };

  return (
    <CurrentUserContext.Provider value={{ currentUser, selectUser }}>{children}</CurrentUserContext.Provider>
  );
};
