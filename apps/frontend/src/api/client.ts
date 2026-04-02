import createClient from "openapi-fetch";
import { runtimeConfig } from "../config/runtimeConfig";
import type { components, paths } from "./api-generated";

type ApiUser = components["schemas"]["BenutzerDto"];
type ApiProblemDetail = components["schemas"]["ProblemDetail"];
type ApiVacationRequest = components["schemas"]["UrlaubsantragDto"];
type ApiVacationRequestHistoryEntry = components["schemas"]["UrlaubsantragStatusHistorieneintragDto"];
type ApiUserTask = components["schemas"]["UserTaskDto"];

const vacationStatuses = [
  "ANTRAG_GESTELLT",
  "AUTOMATISCHE_PRUEFUNG",
  "VORGESETZTEN_PRUEFUNG",
  "ABGELEHNT",
  "GENEHMIGT",
] as const;

export type VacationStatus = (typeof vacationStatuses)[number];

export type UserSummary = {
  name: string;
  email: string;
};

export type VacationRequestHistoryEntry = {
  status: VacationStatus;
  kommentar: string | null;
};

export type VacationRequest = {
  id: string;
  von: string;
  bis: string;
  antragsteller: UserSummary;
  vertretung: UserSummary | null;
  vorgesetzter: UserSummary | null;
  status: VacationStatus;
  statusHistorie: VacationRequestHistoryEntry[];
};

export type VacationRequestInput = {
  antragstellerId: string;
  bis: string;
  von: string;
  vertretungId?: string;
};

export type UserTask = {
  taskId: string;
  urlaubsantrag: VacationRequest | null;
  candidateUsers: UserSummary[];
  bearbeiter: UserSummary | null;
};

export type ManagerDecisionInput = {
  genehmigt: boolean;
  kommentar?: string;
};

export class ApiError extends Error {
  readonly status: number;
  readonly url: string;
  readonly problem: ApiProblemDetail | undefined;

  constructor(options: { status: number; url: string; message: string; problem?: ApiProblemDetail | undefined }) {
    super(options.message);
    this.name = "ApiError";
    this.status = options.status;
    this.url = options.url;
    this.problem = options.problem;
  }
}

const apiClient = createClient<paths>({
  baseUrl: runtimeConfig.apiBaseUrl,
});

const isVacationStatus = (value: unknown): value is VacationStatus =>
  typeof value === "string" && (vacationStatuses as readonly string[]).includes(value);

const normalizeVacationStatus = (value: unknown): VacationStatus =>
  isVacationStatus(value) ? value : "ANTRAG_GESTELLT";

const normalizeUser = (user?: ApiUser): UserSummary => ({
  name: user?.name ?? "Unbekannt",
  email: user?.email ?? "",
});

const normalizeOptionalUser = (user?: ApiUser): UserSummary | null => (user ? normalizeUser(user) : null);

const normalizeVacationRequestHistoryEntry = (
  historyEntry?: ApiVacationRequestHistoryEntry,
): VacationRequestHistoryEntry => ({
  status: normalizeVacationStatus(historyEntry?.status),
  kommentar: historyEntry?.kommentar ?? null,
});

const normalizeVacationRequest = (vacationRequest?: ApiVacationRequest): VacationRequest => ({
  id: vacationRequest?.id ?? "",
  von: vacationRequest?.von ?? "",
  bis: vacationRequest?.bis ?? "",
  antragsteller: normalizeUser(vacationRequest?.antragsteller),
  vertretung: normalizeOptionalUser(vacationRequest?.vertretung),
  vorgesetzter: normalizeOptionalUser(vacationRequest?.vorgesetzter),
  status: normalizeVacationStatus(vacationRequest?.status),
  statusHistorie: (vacationRequest?.statusHistorie ?? []).map(normalizeVacationRequestHistoryEntry),
});

const normalizeUserTask = (userTask?: ApiUserTask): UserTask => ({
  taskId: userTask?.taskId ?? "",
  urlaubsantrag: userTask?.urlaubsantrag ? normalizeVacationRequest(userTask.urlaubsantrag) : null,
  candidateUsers: (userTask?.candidateUsers ?? []).map(normalizeUser),
  bearbeiter: normalizeOptionalUser(userTask?.bearbeiter),
});

const getProblemMessage = (problem: unknown) => {
  if (!problem || typeof problem !== "object") {
    return undefined;
  }

  const detail = (problem as { detail?: unknown }).detail;
  if (typeof detail === "string" && detail.length > 0) {
    return detail;
  }

  const title = (problem as { title?: unknown }).title;
  if (typeof title === "string" && title.length > 0) {
    return title;
  }

  return undefined;
};

const toApiError = (response: Response, problem?: ApiProblemDetail) =>
  new ApiError({
    status: response.status,
    url: response.url,
    message: getProblemMessage(problem) ?? `${response.status} ${response.statusText}`,
    problem,
  });

const expectData = <T>(result: { data?: T; error?: unknown; response: Response }) => {
  if (result.error) {
    throw toApiError(result.response, result.error as ApiProblemDetail);
  }

  if (typeof result.data === "undefined") {
    throw new ApiError({
      status: result.response.status,
      url: result.response.url,
      message: "Die API hat keine Daten zurueckgegeben.",
    });
  }

  return result.data;
};

const expectNoContent = (result: { error?: unknown; response: Response }) => {
  if (result.error) {
    throw toApiError(result.response, result.error as ApiProblemDetail);
  }
};

export const listVacationRequests = async (): Promise<VacationRequest[]> => {
  const result = await apiClient.GET("/api/urlaubsantraege");
  return expectData(result).map(normalizeVacationRequest);
};

export const createVacationRequest = async (body: VacationRequestInput): Promise<VacationRequest> => {
  const result = await apiClient.POST("/api/urlaubsantraege", { body });
  return normalizeVacationRequest(expectData(result));
};

export const listTasks = async (): Promise<UserTask[]> => {
  const result = await apiClient.GET("/api/tasks");
  return expectData(result).map(normalizeUserTask);
};

export const getTask = async (taskId: string): Promise<UserTask> => {
  const result = await apiClient.GET("/api/tasks/{taskId}", {
    params: {
      path: {
        taskId,
      },
    },
  });

  return normalizeUserTask(expectData(result));
};

export const assignTaskToMe = async (taskId: string): Promise<void> => {
  const result = await apiClient.POST("/api/tasks/{taskId}/zuweisen", {
    params: {
      path: {
        taskId,
      },
    },
  });

  expectNoContent(result);
};

export const submitManagerDecision = async (taskId: string, body: ManagerDecisionInput): Promise<void> => {
  const result = await apiClient.POST("/api/tasks/{taskId}/vorgesetztenentscheidung", {
    params: {
      path: {
        taskId,
      },
    },
    body,
  });

  expectNoContent(result);
};
