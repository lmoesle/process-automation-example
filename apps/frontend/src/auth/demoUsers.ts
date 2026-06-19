export type DemoUser = {
  id: string;
  username: string;
  password: string;
  name: string;
  email: string;
  initials: string;
};

const SELECTED_USER_STORAGE_KEY = "process-automation-example.selected-user";

// Demo-only users with hardcoded credentials for quickly switching perspectives in the UI.
// This intentionally is not secure user management and must not be reused for production auth.
export const demoUsers: [DemoUser, ...DemoUser[]] = [
  {
    id: "41f60f4f-1bbb-4469-871f-bf102c46d001",
    username: "john",
    password: "test",
    name: "John",
    email: "john@example.com",
    initials: "JO",
  },
  {
    id: "45a65ce0-5ee9-4b40-bc7d-134837cf3002",
    username: "jane",
    password: "test",
    name: "Jane",
    email: "jane@example.com",
    initials: "JA",
  },
  {
    id: "cd4346cb-e8dc-4ba8-8f94-4f3e5d5ec003",
    username: "max",
    password: "test",
    name: "Max",
    email: "max@example.com",
    initials: "MA",
  },
];

const defaultUser = demoUsers[0];

const findUser = (username: string | null): DemoUser =>
  demoUsers.find((user) => user.username === username) ?? defaultUser;

const canUseStorage = () => typeof window !== "undefined" && Boolean(window.localStorage);

export const loadSelectedUser = (): DemoUser => {
  if (!canUseStorage()) {
    return defaultUser;
  }

  return findUser(window.localStorage.getItem(SELECTED_USER_STORAGE_KEY));
};

export const storeSelectedUser = (user: DemoUser) => {
  if (canUseStorage()) {
    window.localStorage.setItem(SELECTED_USER_STORAGE_KEY, user.username);
  }
};

export const getBasicAuthHeader = (user: DemoUser) => {
  // Demo-only Basic Auth header; credentials are visible in frontend code by design for this example.
  return `Basic ${window.btoa(`${user.username}:${user.password}`)}`;
};
