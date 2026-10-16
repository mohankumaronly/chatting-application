import { create } from "zustand";
import { persist } from "zustand/middleware";

type RegistrationState = {
  registrationId: string | null;
  challengeToken: string | null;
  email: string | null;
  dateOfBirth: string | null;
  otpVerified: boolean;
  profileCompleted: boolean;
  usernameSet: boolean;

  // actions
  setStartData: (data: {
    registrationId: string;
    challengeToken: string;
    email: string;
    dateOfBirth: string;
  }) => void;
  setOtpVerified: (verified: boolean) => void;
  setProfileCompleted: (done: boolean) => void;
  setUsernameSet: (done: boolean) => void;
  setChallengeToken: (token: string) => void;
  reset: () => void;
};

const initialState = {
  registrationId: null,
  challengeToken: null,
  email: null,
  dateOfBirth: null,
  otpVerified: false,
  profileCompleted: false,
  usernameSet: false,
};

export const useRegistrationStore = create<RegistrationState>()(
  persist(
    (set) => ({
      ...initialState,
      setStartData: (data) =>
        set({
          registrationId: data.registrationId,
          challengeToken: data.challengeToken,
          email: data.email,
          dateOfBirth: data.dateOfBirth,
          otpVerified: false,
          profileCompleted: false,
          usernameSet: false,
        }),
      setOtpVerified: (verified) => set({ otpVerified: verified }),
      setProfileCompleted: (done) => set({ profileCompleted: done }),
      setUsernameSet: (done) => set({ usernameSet: done }),
      setChallengeToken: (token) => set({ challengeToken: token }),
      reset: () => set(initialState),
    }),
    {
      name: "chatapp-registration",
      // persist so a page refresh doesn't lose progress
    }
  )
);