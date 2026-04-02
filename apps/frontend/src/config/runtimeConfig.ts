const DEFAULT_API_BASE_URL = "/";
const DEFAULT_APPLICANT_ID = "2d88b39b-e7b0-4a3f-b9c6-b3d8e6fbe100";

export type RuntimeConfig = {
  apiBaseUrl: string;
  applicantId: string;
};

const normalizeBaseUrl = (value?: string) => {
  if (!value || value === "/") {
    return DEFAULT_API_BASE_URL;
  }

  return value.endsWith("/") ? value.slice(0, -1) : value;
};

export const runtimeConfig: RuntimeConfig = {
  apiBaseUrl: normalizeBaseUrl(import.meta.env.VITE_API_BASE_URL),
  applicantId: import.meta.env.VITE_DEMO_APPLICANT_ID ?? DEFAULT_APPLICANT_ID,
};
