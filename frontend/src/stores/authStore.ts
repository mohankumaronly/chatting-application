import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { UserProfile } from "../types/auth";

type AuthState = {
  user: UserProfile | null;
  accessToken: string | null;
  refreshToken: string | null;
  /** true once we've tried to hydrate from storage /me */
  bootstrapped: boolean;

  setTokens: (tokens: { accessToken: string; refreshToken: string }) => void;
  setUser: (user: UserProfile | null) => void;
  setBootstrapped: (done: boolean) => void;
  clear: () => void;
  isAuthenticated: () => boolean;
};

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      bootstrapped: false,

      setTokens: ({ accessToken, refreshToken }) =>
        set({ accessToken, refreshToken }),

      setUser: (user) => set({ user }),

      setBootstrapped: (done) => set({ bootstrapped: done }),

      clear: () =>
        set({
          user: null,
          accessToken: null,
          refreshToken: null,
          bootstrapped: true,
        }),

      isAuthenticated: () => !!get().accessToken,
    }),
    {
      name: "chatapp-auth",
      partialize: (state) => ({
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
      }),
    }
  )
);