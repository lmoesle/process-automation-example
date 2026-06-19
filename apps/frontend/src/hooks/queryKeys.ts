export const queryKeys = {
  users: (username: string) => ["users", username] as const,
  vacationRequests: (username: string) => ["vacation-requests", username] as const,
  tasks: (username: string) => ["tasks", username] as const,
  task: (username: string, taskId: string) => ["tasks", username, taskId] as const,
};
