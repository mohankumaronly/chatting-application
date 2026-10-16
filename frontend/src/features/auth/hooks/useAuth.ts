import { useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../../stores/authStore";
import { authApi } from "../api/authApi";

export function useAuth() {
  const navigate = useNavigate();
  const {
    user,
    accessToken,
    refreshToken,
    setTokens,
    setUser,
    clear,
  } = useAuthStore();

  const isAuthenticated = !!accessToken;

  const login = useCallback(
    async (identifier: string, password: string, keepLoggedIn = true) => {
      const res = await authApi.login({
        identifier,
        password,
        keepLoggedIn,
        clientType: "WEB",
      });
      setTokens({
        accessToken: res.accessToken,
        refreshToken: res.refreshToken,
      });
      if (res.user) {
        setUser(res.user);
      } else {
        // If backend doesn't return user on login, fetch it
        try {
          const me = await authApi.me();
          setUser(me);
        } catch {
          setUser(null);
        }
      }
      return res;
    },
    [setTokens, setUser]
  );

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // Even if server-side logout fails, clear local state
    } finally {
      clear();
      navigate("/login", { replace: true });
    }
  }, [clear, navigate]);

  const bootstrap = useCallback(async () => {
    if (!accessToken) return;
    try {
      const me = await authApi.me();
      setUser(me);
    } catch {
      // interceptor already tried refresh; if still failing, clear
      clear();
    }
  }, [accessToken, setUser, clear]);

  return {
    user,
    accessToken,
    refreshToken,
    isAuthenticated,
    login,
    logout,
    bootstrap,
  };
}