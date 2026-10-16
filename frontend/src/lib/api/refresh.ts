import axios from "axios";

/**
 * A bare axios instance used ONLY for refreshing tokens.
 * It must NOT use apiClient (which has interceptors) to avoid recursion.
 */
const refreshClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
});

type RefreshResult = { accessToken: string; refreshToken: string };

let refreshPromise: Promise<RefreshResult> | null = null;

/**
 * De-duplicated refresh. If 5 requests fire 401 at once, we call
 * /auth/refresh exactly once and share the promise.
 */
export async function refreshTokens(refreshToken: string): Promise<RefreshResult> {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    try {
      const { data } = await refreshClient.post("/api/v1/auth/refresh", {
        refreshToken,
      });
      return {
        accessToken: data.accessToken,
        refreshToken: data.refreshToken ?? refreshToken,
      };
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}