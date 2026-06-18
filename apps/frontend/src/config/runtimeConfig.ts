const DEFAULT_API_BASE_URL = "/";

export type RuntimeConfig = {
  apiBaseUrl: string;
};

const normalizeBaseUrl = (value?: string) => {
  if (!value || value === "/") {
    return DEFAULT_API_BASE_URL;
  }

  return value.endsWith("/") ? value.slice(0, -1) : value;
};

export const runtimeConfig: RuntimeConfig = {
  apiBaseUrl: normalizeBaseUrl(import.meta.env.VITE_API_BASE_URL),
};
