import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CheckCircle2 } from "lucide-react";
import { motion } from "framer-motion";
import { AuthLayout } from "../components/AuthLayout";
import { Button } from "../../../components/ui/Button";
import { authApi } from "../api/authApi";
import { useRegistrationStore } from "../../../stores/registrationStore";
import { useAuthStore } from "../../../stores/authStore";

const RegisterFinishPage = () => {
  const navigate = useNavigate();
  const { registrationId, challengeToken, reset } = useRegistrationStore();

  const setTokens = useAuthStore((s) => s.setTokens);
  const setUser = useAuthStore((s) => s.setUser);
  const setBootstrapped = useAuthStore((s) => s.setBootstrapped);

  const [status, setStatus] = useState<"loading" | "done" | "error">("loading");
  const [error, setError] = useState<string | null>(null);
  const firedRef = useRef(false);

  useEffect(() => {
    if (firedRef.current) return;
    firedRef.current = true;

    (async () => {
      if (!registrationId || !challengeToken) {
        setError("Session expired. Please start over.");
        setStatus("error");
        return;
      }

      try {
        const res = await authApi.finishRegistration(
          registrationId,
          challengeToken,
          { skipOptionalProfile: true }
        );

        // 1. Persist tokens
        setTokens({
          accessToken: res.accessToken,
          refreshToken: res.refreshToken,
        });

        // 2. Hydrate user profile
        try {
          const me = await authApi.me();
          setUser(me);
        } catch {
          setUser(null);
        }

        setBootstrapped(true);

        // 3. Clear registration state (no longer needed)
        reset();

        setStatus("done");
        setTimeout(() => navigate("/app", { replace: true }), 1800);
      } catch (err) {
        setError((err as Error).message);
        setStatus("error");
      }
    })();
  }, [
    registrationId,
    challengeToken,
    reset,
    navigate,
    setTokens,
    setUser,
    setBootstrapped,
  ]);

  return (
    <AuthLayout step={5} title="Almost there">
      <div className="text-center py-4">
        {status === "loading" && (
          <>
            <motion.div
              className="mx-auto w-16 h-16 rounded-full border-4 border-primary-200 border-t-primary-600"
              animate={{ rotate: 360 }}
              transition={{ duration: 0.9, repeat: Infinity, ease: "linear" }}
            />
            <p className="mt-6 text-text-secondary">
              Activating your account…
            </p>
          </>
        )}

        {status === "done" && (
          <>
            <motion.div
              initial={{ scale: 0.5, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ type: "spring", stiffness: 200, damping: 15 }}
              className="mx-auto w-16 h-16 rounded-full bg-green-50 text-success flex items-center justify-center"
            >
              <CheckCircle2 className="w-9 h-9" />
            </motion.div>
            <h2 className="mt-6 text-xl font-semibold text-text-primary">
              Welcome aboard!
            </h2>
            <p className="mt-1 text-text-secondary">
              Your account is ready. Redirecting…
            </p>
          </>
        )}

        {status === "error" && (
          <>
            <div className="mx-auto w-16 h-16 rounded-full bg-red-50 text-danger flex items-center justify-center text-3xl font-bold">
              !
            </div>
            <h2 className="mt-6 text-xl font-semibold text-text-primary">
              Something went wrong
            </h2>
            <p className="mt-1 text-sm text-danger">{error}</p>
            <div className="mt-6">
              <Button onClick={() => navigate("/register")} variant="secondary">
                Start over
              </Button>
            </div>
          </>
        )}
      </div>
    </AuthLayout>
  );
};

export default RegisterFinishPage;