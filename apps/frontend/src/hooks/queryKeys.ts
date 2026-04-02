export const queryKeys = {
  vacationRequests: ["vacation-requests"] as const,
  tasks: ["tasks"] as const,
  task: (taskId: string) => ["tasks", taskId] as const,
};
