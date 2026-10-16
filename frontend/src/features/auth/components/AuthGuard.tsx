import { useEffect } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuthStore } from "../../../stores/authStore";
import { useAuth } from "../hooks/useAuth";

/**
 * Wrap any route you want to protect.
 * If not authenticated → redirect to /login with `from` state.
 * On first mount with a token → hydrate user via /me.
 */
export const AuthGuard = ({ children }: { children: React.ReactNode }) => {
  const location = useLocation();
  const { accessToken, bootstrapped, setBootstrapped, user } = useAuthStore();
  const { bootstrap } = useAuth();

  useEffect(() => {
    if (accessToken && !bootstrapped) {
      bootstrap().finally(() => setBootstrapped(true));
    }
  }, [accessToken, bootstrapped, bootstrap, setBootstrapped]);

  if (!accessToken) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  // If we have a token but haven't hydrated the user yet, wait
  if (!bootstrapped || !user) {
    return (
      <div className="h-screen flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 rounded-full border-2 border-primary-200 border-t-primary-600 animate-spin" />
          <span className="text-sm text-text-secondary">Loading…</span>
        </div>
      </div>
    );
  }

  return <>{children}</>;
};