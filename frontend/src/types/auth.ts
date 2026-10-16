export type StartRegistrationRequest = {
  email: string;
  dateOfBirth: string;
};

export type StartRegistrationResponse = {
  registrationId: string;
  challengeToken: string;
  message?: string;
  expiresAt?: string;
};

export type VerifyOtpRequest = {
  otp: string;
};

export type VerifyOtpResponse = {
  verified: boolean;
  message?: string;
};

export type CompleteRegistrationRequest = {
  firstName: string;
  lastName: string;
  password: string;
  keepLoggedIn: boolean;
  clientType: "WEB" | "ANDROID" | "IOS";
};

export type SetUsernameRequest = {
  username: string;
};

export type FinishRegistrationRequest = {
  skipOptionalProfile: boolean;
};

export type AuthTokens = {
  accessToken: string;
  refreshToken: string;
};

export type FinishRegistrationResponse = AuthTokens & {
  userId?: string;
  message?: string;
};

export type LoginRequest = {
  identifier: string; // email or username
  password: string;
  keepLoggedIn: boolean;
  clientType: "WEB" | "ANDROID" | "IOS";
};

export type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  user?: UserProfile;
  expiresIn?: number;
};

export type RefreshResponse = {
  accessToken: string;
  refreshToken: string;
};

export type UserProfile = {
  id: string;
  email: string;
  username: string | null;
  firstName: string | null;
  lastName: string | null;
  bio?: string | null;
  avatarUrl?: string | null;
  avatarPublicId?: string | null;
  emailVerified?: boolean;
  createdAt?: string;
};