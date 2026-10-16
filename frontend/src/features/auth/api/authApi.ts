import { apiClient } from "../../../lib/api/client";
import type {
  StartRegistrationRequest,
  StartRegistrationResponse,
  VerifyOtpRequest,
  VerifyOtpResponse,
  CompleteRegistrationRequest,
  SetUsernameRequest,
  FinishRegistrationRequest,
  FinishRegistrationResponse,
  LoginRequest,
  LoginResponse,
  RefreshResponse,
  UserProfile,
} from "../../../types/auth";

export const authApi = {
  /* ============================================================
   * Registration flow (5 steps)
   * ============================================================ */

  startRegistration: async (
    payload: StartRegistrationRequest
  ): Promise<StartRegistrationResponse> => {
    const { data } = await apiClient.post(
      "/api/v1/auth/registrations",
      payload
    );
    return data;
  },

  resendOtp: async (registrationId: string, challengeToken: string) => {
    const { data } = await apiClient.post(
      `/api/v1/auth/registrations/${registrationId}/otp/resend`,
      {},
      { headers: { "X-Challenge-Token": challengeToken } }
    );
    return data;
  },

  verifyOtp: async (
    registrationId: string,
    challengeToken: string,
    payload: VerifyOtpRequest
  ): Promise<VerifyOtpResponse> => {
    const { data } = await apiClient.post(
      `/api/v1/auth/registrations/${registrationId}/otp/verify`,
      payload,
      { headers: { "X-Challenge-Token": challengeToken } }
    );
    return data;
  },

  completeRegistration: async (
    registrationId: string,
    challengeToken: string,
    payload: CompleteRegistrationRequest
  ) => {
    const { data } = await apiClient.post(
      `/api/v1/auth/registrations/${registrationId}/complete`,
      payload,
      { headers: { "X-Challenge-Token": challengeToken } }
    );
    return data;
  },

  setUsername: async (
    registrationId: string,
    challengeToken: string,
    payload: SetUsernameRequest
  ) => {
    const { data } = await apiClient.post(
      `/api/v1/auth/registrations/${registrationId}/username`,
      payload,
      { headers: { "X-Challenge-Token": challengeToken } }
    );
    return data;
  },

  checkUsernameAvailable: async (username: string) => {
    const { data } = await apiClient.get("/api/v1/username/available", {
      params: { username },
    });
    return data as { available: boolean };
  },

  finishRegistration: async (
    registrationId: string,
    challengeToken: string,
    payload: FinishRegistrationRequest
  ): Promise<FinishRegistrationResponse> => {
    const { data } = await apiClient.post(
      `/api/v1/auth/registrations/${registrationId}/finish`,
      payload,
      { headers: { "X-Challenge-Token": challengeToken } }
    );
    return data;
  },

  /* ============================================================
   * Authentication & Session
   * ============================================================ */

  login: async (payload: LoginRequest): Promise<LoginResponse> => {
    const { data } = await apiClient.post("/api/v1/auth/login", payload, {
      skipAuth: true,
    } as never);
    return data;
  },

  logout: async () => {
    const { data } = await apiClient.post("/api/v1/auth/logout");
    return data;
  },

  logoutAll: async () => {
    const { data } = await apiClient.post("/api/v1/auth/logout-all");
    return data;
  },

  me: async (): Promise<UserProfile> => {
    const { data } = await apiClient.get("/api/v1/auth/me");
    return data;
  },

  refresh: async (refreshToken: string): Promise<RefreshResponse> => {
    const { data } = await apiClient.post(
      "/api/v1/auth/refresh",
      { refreshToken },
      { skipAuth: true } as never
    );
    return data;
  },
};