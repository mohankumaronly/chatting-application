import { Navigate, useLocation } from "react-router-dom";
import { useRegistrationStore } from "../../../stores/registrationStore";

type Step = "start" | "otp" | "complete" | "username" | "finish";

type Props = {
  requires: Step;
  children: React.ReactNode;
};

export const RegistrationGuard = ({ requires, children }: Props) => {
  const location = useLocation();
  const {
    registrationId,
    otpVerified,
    profileCompleted,
    usernameSet,
  } = useRegistrationStore();

  const earliestMissing: Step | null = !registrationId
    ? "start"
    : !otpVerified
      ? "otp"
      : !profileCompleted
        ? "complete"
        : !usernameSet
          ? "username"
          : "finish";

  const satisfied =
    requires === "start"
      ? !!registrationId
      : requires === "otp"
        ? otpVerified
        : requires === "complete"
          ? profileCompleted
          : requires === "username"
            ? usernameSet
            : true;

  if (requires === "start" && registrationId && !otpVerified) {
    return <Navigate to="/register/otp" replace state={{ from: location }} />;
  }

  if (requires !== "start" && earliestMissing && earliestMissing !== requires) {
    const routeMap: Record<Step, string> = {
      start: "/register",
      otp: "/register/otp",
      complete: "/register/complete",
      username: "/register/username",
      finish: "/register/finish",
    };
    return <Navigate to={routeMap[earliestMissing]} replace />;
  }

  if (satisfied && requires !== "finish") {
    const nextMap: Record<Step, string> = {
      start: "/register/otp",
      otp: "/register/complete",
      complete: "/register/username",
      username: "/register/finish",
      finish: "/register/finish",
    };
    return <Navigate to={nextMap[requires]} replace />;
  }

  return <>{children}</>;
};