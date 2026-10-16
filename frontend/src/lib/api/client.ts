import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";
import { useAuthStore } from "../../stores/authStore";
import { refreshTokens } from "./refresh";

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
});

/* ---------- Request interceptor: attach Bearer token ---------- */
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = useAuthStore.getState().accessToken;
  const skipAuth = (config as { skipAuth?: boolean }).skipAuth;
  if (token && !skipAuth) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/* ---------- Response interceptor: 401 → refresh → retry ---------- */
type RetriableConfig = InternalAxiosRequestConfig & { _retry?: boolean };

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as RetriableConfig | undefined;

    // Only attempt refresh when:
    //  - status is 401
    //  - we haven't already retried this request
    //  - it's not the refresh endpoint itself (avoid infinite loop)
    const isRefreshCall = original?.url?.includes("/auth/refresh");
    const shouldRefresh =
      error.response?.status === 401 &&
      original &&
      !original._retry &&
      !isRefreshCall;

    if (shouldRefresh && original) {
      original._retry = true;
      const { refreshToken, setTokens, clear } = useAuthStore.getState();

      if (!refreshToken) {
        clear();
        return Promise.reject(normalize(error));
      }

      try {
        const tokens = await refreshTokens(refreshToken);
        setTokens(tokens);
        original.headers.Authorization = `Bearer ${tokens.accessToken}`;
        return apiClient(original);
      } catch (refreshErr) {
        clear();
        // bubble up — AuthGuard will redirect
        return Promise.reject(normalize(refreshErr as AxiosError));
      }
    }

    return Promise.reject(normalize(error));
  }
);

/* ---------- Error normalizer ---------- */
function normalize(error: AxiosError): Error {
  const data = error?.response?.data as
    | { message?: string; error?: string; detail?: string }
    | undefined;

  if (import.meta.env.DEV) {
    console.error("[API ERROR]", {
      url: error?.config?.url,
      method: error?.config?.method,
      status: error?.response?.status,
      body: data,
    });
  }

  const message =
    data?.message ||
    data?.error ||
    data?.detail ||
    error?.message ||
    "Something went wrong. Please try again.";

  return new Error(message);
}